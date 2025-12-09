package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.CancelCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.IssueCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.event.CouponIssueEvent;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.event.OrderCancelledEvent;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.event.OrderRollbackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponEventConsumer {
    private final IssueCouponService issueCouponService;

    @KafkaListener(
        topics = "${spring.kafka.topics.order}",
        groupId = "coupon",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, Object> record) {

        String key = record.key();
        String type = new String(record.headers().lastHeader("eventType").value());
        Object value = record.value();

        log.info("Received message: type={}, key={}, value={}", type, key, value);

        switch (type) {
            case "ISSUE_COUPON" -> handleIssueCoupon(value);
            case "ORDER_ROLLEDBACK" -> handleRollback(value);
            case "ORDER_CANCELLED" -> handleCancel(value);
            default -> throw new RuntimeException("Unexpected message type: " + type);
        }
    }

    private void handleIssueCoupon(Object value) {
        CouponIssueEvent event = (CouponIssueEvent) value;

        log.info("[issueCoupon] event: {}", event);

        UUID couponId = event.couponId();
        Long userId = event.userId();

        if (!issueCouponService.existsByCouponIdAndUserId(couponId, userId)) {
            try {
                issueCouponService.issue(
                    new IssueCouponCommand(couponId, userId)
                );
            } catch (DataIntegrityViolationException e) {
                log.warn("중복 발급 시도.");
            }
        } else {
            throw new IllegalArgumentException("이미 발급 받은 유저입니다.");
        }
    }

    private void handleRollback(Object value) {
        OrderRollbackEvent event = (OrderRollbackEvent) value;

        log.info("[rollback] event: {}", event);

        issueCouponService.cancelCoupon(
            new CancelCouponCommand(event.orderId())
        );
    }

    private void handleCancel(Object value) {
        OrderCancelledEvent event = (OrderCancelledEvent) value;

        log.info("[cancel] event: {}", event);

        issueCouponService.cancelCoupon(
            new CancelCouponCommand(event.orderId())
        );
    }
}
