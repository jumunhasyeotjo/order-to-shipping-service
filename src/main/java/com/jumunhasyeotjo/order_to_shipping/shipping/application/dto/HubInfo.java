package com.jumunhasyeotjo.order_to_shipping.shipping.application.dto;

import java.util.UUID;

public record HubInfo(
	UUID hubId,
	String hubName
) {
}
