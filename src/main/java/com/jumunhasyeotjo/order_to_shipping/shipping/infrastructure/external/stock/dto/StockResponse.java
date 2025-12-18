package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock.dto;

import java.util.UUID;

public record StockResponse(
	UUID stockId,
	UUID productId,
	UUID hubId,
	Integer quantity
) {
}
