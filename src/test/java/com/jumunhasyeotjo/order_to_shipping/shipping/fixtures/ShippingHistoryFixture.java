package com.jumunhasyeotjo.order_to_shipping.shipping.fixtures;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;

public class ShippingHistoryFixture {

	public static ShippingHistory createDefault(){
		Shipping shipping = ShippingFixture.createDefault();
		Long driverId = 1L;
		Integer sequence = 1;
		String origin = "출발지";
		String destination = "도착";
		RouteInfo expectRouteInfo = RouteInfo.of(100, 100);

		return ShippingHistory.create(shipping, driverId, sequence, origin, destination, expectRouteInfo);
	}

	public static ShippingHistory createWithShippingAndSequence(Shipping shipping, Integer sequence){
		Long driverId =1L;
		String origin = "출발지";
		String destination = "도착지";
		RouteInfo expectRouteInfo = RouteInfo.of(100, 100);

		return ShippingHistory.create(shipping, driverId, sequence, origin, destination, expectRouteInfo);
	}
}
