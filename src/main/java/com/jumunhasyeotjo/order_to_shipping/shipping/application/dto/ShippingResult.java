package com.jumunhasyeotjo.order_to_shipping.shipping.application.dto;

import java.util.List;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

public record ShippingResult(
	Shipping shipping,
	List<ShippingHistory> shippingHistories
) {
	public static ShippingResult from(Shipping shipping, List<ShippingHistory> shippingHistories){
		return new ShippingResult(shipping, shippingHistories);
	}
}
