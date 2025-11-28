package com.jumunhasyeotjo.order_to_shipping.order.application.event;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.StockApiReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCanceledEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderRolledBackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final StockClient stockClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Todo : 배송 이벤트 리스너에서 받아서 처리
//        deliveryClient.createDelivery(
//                event.getOrderId(),
//                event.getSlackId(),
//                event.getOrderCreatedTime(),
//                event.getProductInfo(),
//                event.getRequestMessage(),
//                event.getReceiverName(),
//                event.getSupplierCompanyId(),
//                event.getReceiverCompanyId()
//        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCanceled(OrderCanceledEvent event) {
        stockClient.restoreStocks(new StockApiReq(event.getOrderProducts()), event.getIdempotentKey());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRolledBack(OrderRolledBackEvent event) {
//        stockClient.rollbackedStocks(event.getOrderProducts(), event.getIdempotentKey());
    }
}
