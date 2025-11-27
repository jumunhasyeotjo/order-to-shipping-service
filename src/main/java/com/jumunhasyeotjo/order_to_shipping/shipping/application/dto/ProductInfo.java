package com.jumunhasyeotjo.order_to_shipping.shipping.application.dto;

import java.util.UUID;

public record ProductInfo(
	UUID productId,
	Integer quantity
) {
}
