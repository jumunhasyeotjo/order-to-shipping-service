package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ShippingDelayedEvent extends ShippingDomainEvent {

    private final UUID shippingId;
    private final UUID receiverCompanyId;
    private final String message;

    public ShippingDelayedEvent(UUID shippingId, UUID receiverCompanyId, String message) {
        this.shippingId = shippingId;
        this.receiverCompanyId = receiverCompanyId;
        this.message = message;
    }

    public static ShippingDelayedEvent of(UUID shippingId, UUID receiverCompanyId, String message) {
        return new ShippingDelayedEvent(shippingId, receiverCompanyId, message);
    }
}
