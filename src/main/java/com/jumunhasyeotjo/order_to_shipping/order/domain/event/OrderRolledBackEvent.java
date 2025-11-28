package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderRolledBackEvent implements DomainEvent {

    private final String idempotentKey;
    private final List<OrderProductReq> orderProducts;

    public OrderRolledBackEvent(String idempotentKey, List<OrderProductReq> orderProducts) {
        this.idempotentKey = idempotentKey;
        this.orderProducts = orderProducts;
    }

    public static OrderRolledBackEvent of(String idempotentKey, List<OrderProductReq> orderProducts) {
        return new OrderRolledBackEvent(
                idempotentKey,
                orderProducts
        );
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return null;
    }
}
