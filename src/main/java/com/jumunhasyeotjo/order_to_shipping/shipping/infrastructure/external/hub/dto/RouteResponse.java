package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

public record RouteResponse(
	UUID routeId,
	UUID startHub,
	UUID endHub,
	Integer durationMinutes,
	Integer distanceKm
) {
}
