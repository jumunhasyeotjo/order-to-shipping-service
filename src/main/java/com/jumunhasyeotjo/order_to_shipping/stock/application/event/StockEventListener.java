package com.jumunhasyeotjo.order_to_shipping.stock.application.event;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.event.PaymentCompletedEvent;
import com.jumunhasyeotjo.order_to_shipping.stock.application.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class StockEventListener {

    private final StockService stockService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("재고 차감 시작 - Thread: {}, orderId: {}, occurredAt: {}",
                Thread.currentThread().getName(),
                event.getOrderId(),
                event.getOccurredAt());

        stockService.decreaseStock(
                event.getOrderId(),
                event.getOrderProducts()
        );

        log.info("재고 차감 완료 - orderId: {}, completedAt: {} ", event.getOrderId(), LocalDateTime.now());
    }
}
