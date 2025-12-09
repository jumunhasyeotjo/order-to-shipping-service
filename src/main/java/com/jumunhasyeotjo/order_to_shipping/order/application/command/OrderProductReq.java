package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.UUID;

import org.aspectj.weaver.ast.Or;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;

public record OrderProductReq(
		UUID hubId,
        UUID productId,
        Integer quantity
) {
	public static OrderProductReq from(ProductInfo productInfo, UUID hubId){
		return new OrderProductReq(
			hubId,
			productInfo.productId(),
			productInfo.quantity()
		);
	}
}
