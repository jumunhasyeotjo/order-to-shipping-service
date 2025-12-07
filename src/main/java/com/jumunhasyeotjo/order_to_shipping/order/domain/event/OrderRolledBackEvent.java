package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderRolledBackEvent implements DomainEvent {

    private final UUID orderId;
    private final RollbackStatus status;
    private final LocalDateTime occurredAt;

    public OrderRolledBackEvent(UUID orderId, RollbackStatus status) {
        this.orderId = orderId;
        this.status = status;
        this.occurredAt = LocalDateTime.now();
    }

    public static OrderRolledBackEvent of(UUID orderId, RollbackStatus status) {
        return new OrderRolledBackEvent(
                orderId,
                status
        );
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return this.occurredAt;
    }
}
