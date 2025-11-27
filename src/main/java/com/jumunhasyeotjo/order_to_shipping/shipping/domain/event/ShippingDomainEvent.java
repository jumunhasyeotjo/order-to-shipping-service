package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public abstract class ShippingDomainEvent {
    private final LocalDateTime occurredAt;
    
    protected ShippingDomainEvent() {
        this.occurredAt = LocalDateTime.now();
    }
}

