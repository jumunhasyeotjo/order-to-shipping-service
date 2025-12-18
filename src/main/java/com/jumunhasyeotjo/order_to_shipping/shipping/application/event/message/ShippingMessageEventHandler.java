package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.NetworkException;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingEtaAiPredictor;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingDelayedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingMsgReqEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.KafkaShippingMessageEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingMessageEventHandler {
	private final ShippingEtaAiPredictor shippingEtaAiPredictor;
	private final KafkaShippingMessageEventPublisher kafkaShippingMessageEventPublisher;

	@Value("${spring.kafka.topic.shipping-events:shipping-message-events}")
	private String SHIPPING_MESSAGE_TOPIC;

	private final ObjectMapper objectMapper;


	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleShippingCreated(ShippingCreatedEvent event) {
		List<ShippingHistory> shippingHistories = event.getShippingHistories();
		String waypoints = buildWaypoints(event.getShippingHistories());
		String eta = shippingEtaAiPredictor.predictEta(
			event.getProductInfo(),
			event.getOrderRequest(),
			shippingHistories,
			waypoints
		);

		String orderIdMessage = "업체 주문 id : " + event.getShippingId();

		String infoMessage = "주문 시간 : " + event.getCreatedAt().format(DateTimeFormatter.ISO_DATE) + "\n"
			+ "상품 정보 : " + event.getProductInfo() + "\n"
			+ "요청 사항 : " + event.getOrderRequest() + "\n"
			+ "발송지 : " + shippingHistories.get(0).getOrigin() + "\n"
			+ "경유지 : " + waypoints + "\n"
			+ "도착지 : " + shippingHistories.get(shippingHistories.size() - 1).getDestination() + "\n";

		String etaMessage = "위 내용을 기반으로 도출된 최종 발송 시한은 " + eta + " 입니다.";

        ShippingMsgReqEvent shippingMsgReqEvent = new ShippingMsgReqEvent(event.getOriginHubId(),
                event.getReceiverCompanyId(), orderIdMessage, infoMessage, etaMessage, event.getDriverId());
        kafkaShippingMessageEventPublisher.publishEvent(shippingMsgReqEvent, "SHIPPING_ETA_MESSAGE");

	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleShippingDelayMessage(ShippingDelayedEvent event) throws JsonProcessingException {
		try {
			String eventType = "SHIPPING_DELAYED";
			ProducerRecord<String, String> record = new ProducerRecord<>(SHIPPING_MESSAGE_TOPIC, objectMapper.writeValueAsString(event));
			record.headers().add(new RecordHeader("eventType", eventType.getBytes(StandardCharsets.UTF_8)));

			kafkaShippingMessageEventPublisher.publishEvent(event,"SHIPPING_DELAYED"); // 예외 캐치를 위해 동기적으로 결과 호출
			log.info("Kafka 발행 성공 ID: {}, Type: {}", event.getShippingId(), eventType);

		} catch (Exception e) {
			log.error("Kafka 발행 실패 ID: {}", event.getShippingId());
			throw new NetworkException("Kafka message send failed", e);
		}
	}


	private String buildWaypoints(List<ShippingHistory> shippingHistories) {
		if (shippingHistories.size() <= 2) {
			return "-";
		}

		StringBuilder waypoints = new StringBuilder();
		for (int i = 1; i < shippingHistories.size() - 1; i++) {
			if (waypoints.length() > 0) {
				waypoints.append(", ");
			}
			waypoints.append(shippingHistories.get(i).getOrigin());
		}
		return waypoints.toString();
	}

	@Recover
	public void recover(NetworkException e, ShippingCreatedEvent event) {
		// 3번 시도해도 실패하면 이 메서드가 호출됨
		log.error("배송 예상 시간 알림 처리 실패 - 배송 ID: {}", event.getShippingId());
	}

	@Recover
	public void recover(NetworkException e, ShippingDelayedEvent event) {
		// Todo : DLT 등의 방식으로 데이터 보관후 추후 배치처리 필요
	}
}