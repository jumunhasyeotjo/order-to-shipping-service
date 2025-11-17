package com.jumunhasyeotjo.order_to_shipping.order.application.event;

import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

//    @EventListener
//    public void handleOrderCreated(OrderCreatedEvent event) {
//        log.info("주문 생성 이벤트 수신 orderID: {}, companyId: {}, totalPrice: {}, timestamp: {}",
//                event.getOrderId(),
//                event.getOrderProducts(),
//                event.getTotalPrice(),
//                event.getOccurredAt());
//    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event){
        log.info("OrderCreatedEvent 발행 성공");
    }
}
