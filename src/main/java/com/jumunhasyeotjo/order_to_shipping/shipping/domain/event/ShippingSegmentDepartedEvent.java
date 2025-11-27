package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

import lombok.Getter;

@Getter
public class ShippingSegmentDepartedEvent extends ShippingDomainEvent{
	private final String origin;
	private final UUID shippingId;
	private final UUID idempotencyKey = UUID.randomUUID();
	private final boolean isFromOriginHub;

	public ShippingSegmentDepartedEvent(String origin, UUID shippingId, boolean isFromOriginHub) {
		this.origin = origin;
		this.shippingId = shippingId;
		this.isFromOriginHub = isFromOriginHub;
	}

}
