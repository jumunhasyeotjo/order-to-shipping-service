package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import java.util.UUID;

import lombok.Getter;

@Getter
public class ShippingSegmentArrivedEvent extends ShippingDomainEvent {
	private final String destination;
	private final UUID shippingId;
	private final UUID idempotencyKey = UUID.randomUUID();
	private final boolean isFinalDestination;

	public ShippingSegmentArrivedEvent(String destination, UUID shippingId, boolean isFinalDestination) {
		this.destination = destination;
		this.shippingId = shippingId;
		this.isFinalDestination = isFinalDestination;
	}
}
