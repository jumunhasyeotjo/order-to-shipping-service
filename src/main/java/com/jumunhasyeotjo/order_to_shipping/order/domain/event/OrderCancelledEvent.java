package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.CancelReason;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderCancelledEvent implements DomainEvent {

    private final UUID orderId;
    private final String cancelReason;
    private final LocalDateTime occurredAt;

    public OrderCancelledEvent(UUID orderId, CancelReason reason) {
        this.orderId = orderId;
        this.cancelReason = reason.getDescription();
        this.occurredAt = LocalDateTime.now();
    }

    public static OrderCancelledEvent of(UUID orderId, CancelReason reason) {
        return new OrderCancelledEvent(
                orderId,
                reason
        );
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return this.occurredAt;
    }
}
