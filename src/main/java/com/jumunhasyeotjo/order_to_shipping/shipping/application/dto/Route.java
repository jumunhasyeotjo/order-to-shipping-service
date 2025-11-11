package com.jumunhasyeotjo.order_to_shipping.shipping.application.dto;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

public record Route(
	UUID departureHubId,
	UUID destinationHubId,
	RouteInfo info
) {
	public static Route of(UUID departureHubId, UUID destinationHubId, RouteInfo info){
		return new Route(departureHubId, destinationHubId, info);
	}
}
