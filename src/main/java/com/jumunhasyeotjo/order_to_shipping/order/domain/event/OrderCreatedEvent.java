package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderCreatedEvent implements DomainEvent {

    private final UUID orderId;
    private final List<OrderProduct> orderProducts;
    private final int totalPrice;
    private final LocalDateTime occurredAt;

    public static OrderCreatedEvent of(Order order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderProducts(),
                order.getTotalPrice(),
                LocalDateTime.now()
        );
    }

    @Override
    public UUID getAggregateId() {
        return orderId;
    }
}
