package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderCancelledEvent implements DomainEvent {

    private final UUID orderId;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(UUID orderId) {
        this.orderId = orderId;
        this.occurredAt = LocalDateTime.now();
    }

    public static OrderCancelledEvent of(UUID orderId) {
        return new OrderCancelledEvent(
                orderId
        );
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return this.occurredAt;
    }
}
