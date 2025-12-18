package com.jumunhasyeotjo.order_to_shipping.shipping.fixtures;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;

public class ShippingFixture {

	public static Shipping createDefault(){
		UUID orderId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		UUID receiverCompanyId = UUID.randomUUID();
		ShippingAddress address = ShippingAddress.of("서울시 강동구");
		UUID originHubId = UUID.randomUUID();
		UUID arrivalHubId = UUID.randomUUID();
		Integer totalRouteCount = 3;

		return Shipping.create(id, orderId, receiverCompanyId, address, originHubId,
			arrivalHubId, totalRouteCount);
	}

}
