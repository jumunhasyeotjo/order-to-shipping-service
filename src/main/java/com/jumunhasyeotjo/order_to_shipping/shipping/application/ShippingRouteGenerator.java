package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

@Service
public class ShippingRouteGenerator {

	public List<Route> generatorRoute(UUID originHubId, UUID arrivalHubId){

		//todo 경로생성 알고리즘
		Route route1 = Route.of(UUID.randomUUID(), UUID.randomUUID(), RouteInfo.of(100, 100));
		Route route2 = Route.of(UUID.randomUUID(), UUID.randomUUID(), RouteInfo.of(100, 100));

		return List.of(route1, route2);
	}
}
