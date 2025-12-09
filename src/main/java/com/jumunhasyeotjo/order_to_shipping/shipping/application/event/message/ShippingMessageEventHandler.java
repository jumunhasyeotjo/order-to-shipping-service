package com.jumunhasyeotjo.order_to_shipping.shipping.application.event;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.NetworkException;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingEtaAiPredictor;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.OrderClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentArrivedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentDepartedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.HubIdCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingEventHandler {
	private final ShippingEtaAiPredictor shippingEtaAiPredictor;
	private final UserClient userClient;

	@Async
	@Retryable(
		value = NetworkException.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000)
	)
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

		userClient.sendSlackMessage(event.getOriginHubId(), event.getReceiverCompanyId(), orderIdMessage, infoMessage,
			etaMessage, event.getDriverId());
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
}