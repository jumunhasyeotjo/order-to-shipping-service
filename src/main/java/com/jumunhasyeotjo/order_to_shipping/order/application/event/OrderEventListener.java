package com.jumunhasyeotjo.order_to_shipping.order.application.event;

import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
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
}
