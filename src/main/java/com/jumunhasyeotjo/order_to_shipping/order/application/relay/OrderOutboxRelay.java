package com.jumunhasyeotjo.order_to_shipping.order.application.relay;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderOutbox;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCancelledEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderRolledBackEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxRelay {

    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.order-events:order}")
    private String ORDER_TOPIC;

    // 배치 처리 사이즈
    private static final int BATCH_SIZE = 10;

    // Real-time 처리: 트랜잭션 커밋 직후 실행
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishImmediately(OrderCreatedEvent event) {
        publishByAggregate(event.getOrderId(), "ORDER_CREATED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishImmediately(OrderRolledBackEvent event) {
        publishByAggregate(event.getOrderId(), "ORDER_ROLLEDBACK");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishImmediately(OrderCancelledEvent event) {
        publishByAggregate(event.getOrderId(), "ORDER_CANCELLED");
    }

    private void publishByAggregate(UUID aggregateId, String eventType) {
        Optional<OrderOutbox> outbox = orderOutboxRepository.findByAggregateIdAndEventTypeAndIsPublishedFalse(aggregateId, eventType);

        // 다른 스레드나 폴링에 의해 처리된 경우
        if (outbox.isEmpty()) {
            return;
        }
        sendKafkaAndUpdateStatus(outbox.get());

    }

    // Polling 처리: 단건 발행시 누락된 이벤트 재발행
    @Scheduled(fixedDelay = 20000)
    public void pollOutbox() {
        Pageable pageable = PageRequest.of(0, BATCH_SIZE, Sort.by("createdAt").ascending());
        List<OrderOutbox> pendingOutboxes = orderOutboxRepository.findAllByIsPublishedFalse(pageable);

        if (!pendingOutboxes.isEmpty()) {
//            log.info("발행 되지않은 Outbox 데이터 {}", pendingOutboxes.size());
            for (OrderOutbox outbox : pendingOutboxes) {
                sendKafkaAndUpdateStatus(outbox);
            }
        }
    }

    // Kafka 발행, 성공 시에만 DB 상태 업데이트
    private void sendKafkaAndUpdateStatus(OrderOutbox outbox) {
        String key = outbox.getAggregateId().toString(); // 파티션 순서 보장을 위해 AggregateId를 Key로 사용
        String payload = outbox.getPayload();

        ProducerRecord<String, String> record = new ProducerRecord<>(ORDER_TOPIC, key, payload);
        record.headers().add(new RecordHeader("eventType", outbox.getEventType().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                outbox.publish();
                orderOutboxRepository.save(outbox);
                log.info("[Event Publish Success] 주문 {} 이벤트 발행 성공 - orderId: {}", outbox.getEventType(), outbox.getId());
            } else {
                log.error("[Event Publish Fail] 이벤트 발행 실패 - orderId: {}", outbox.getId(), ex);
            }
        });
    }
}