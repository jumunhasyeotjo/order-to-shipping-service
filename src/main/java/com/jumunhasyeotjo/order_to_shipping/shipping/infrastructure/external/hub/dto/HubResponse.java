package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto;

import java.util.UUID;

public record HubResponse(
	UUID id,
	String name,
	String address,
	Double latitude,
	Double longitude
) {
}
