package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.HubInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.HubServiceClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubClientImpl implements HubClient{
	private final HubServiceClient hubServiceClient;

	@Override
	public List<Route> getRoutes() {
		return hubServiceClient.getRoutes().stream()
			.map(response -> new Route(
				response.startHub(),
				response.endHub(),
				RouteInfo.of(response.distanceKm(), response.durationMinutes())
			))
			.toList();
	}

	@Override
	public List<HubInfo> getAllHubs() {
		return hubServiceClient.getAllHubs().stream()
			.map(response -> new HubInfo(
				response.id(),
				response.name()
			)).toList();
	}
}
