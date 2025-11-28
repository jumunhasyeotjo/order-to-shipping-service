package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderCanceledEvent implements DomainEvent {

    private final String idempotentKey;
    private final List<OrderProductReq> orderProducts;

    public OrderCanceledEvent(String idempotentKey, List<OrderProductReq> orderProducts) {
        this.idempotentKey = idempotentKey;
        this.orderProducts = orderProducts;
    }

    public static OrderCanceledEvent of(String idempotentKey, Order order) {
        return new OrderCanceledEvent(
                idempotentKey,
                order.getOrderCompanies().stream()
                        .flatMap(company -> company.getOrderProducts().stream()
                                .map(p -> new OrderProductReq(p.getProductId(), p.getQuantity())))
                        .toList()
        );
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return null;
    }
}
