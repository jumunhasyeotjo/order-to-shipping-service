package com.jumunhasyeotjo.order_to_shipping.payment.application.event;

import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.payment.application.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedAsync(OrderCreatedEvent event) {
        log.info("비동기 결제 처리 시작 - Thread: {}, orderId: {}, occurredAt: {}",
                Thread.currentThread().getName(),
                event.getOrderId(),
                event.getOccurredAt());

        paymentService.processPayment(
                event.getOrderId(),
                event.getTotalPrice(),
                event.getOrderProducts()
        );

        log.info("비동기 결제 처리 완료 - orderId: {}, completedAt: {} ", event.getOrderId(), LocalDateTime.now());
    }
}
