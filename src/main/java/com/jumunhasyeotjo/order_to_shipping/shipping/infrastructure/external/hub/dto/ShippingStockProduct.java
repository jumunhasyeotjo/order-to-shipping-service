package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;

public record ShippingStockProduct(
		UUID hubId,
        UUID productId,
        Integer quantity
) {
	public static ShippingStockProduct from(ProductInfo productInfo, UUID hubId){
		return new ShippingStockProduct(
			hubId,
			productInfo.productId(),
			productInfo.quantity()
		);
	}
}
