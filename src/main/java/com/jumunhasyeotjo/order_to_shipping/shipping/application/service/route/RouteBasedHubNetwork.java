package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;

public class RouteBasedHubNetwork implements HubNetwork {

	private final Map<UUID, Set<UUID>> adjacency;
	private final Map<String, EdgeWeight> edgeWeights;

	public RouteBasedHubNetwork(List<Route> routes) {
		this.adjacency = routes.stream()
			.flatMap(route -> Stream.of(
				Map.entry(route.departureHubId(), route.destinationHubId()),
				Map.entry(route.destinationHubId(), route.departureHubId())
			))
			.collect(Collectors.groupingBy(
				Map.Entry::getKey,
				Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
			));

		// 가중치 저장 (양방향)
		this.edgeWeights = new HashMap<>();
		for (Route route : routes) {
			String key1 = key(route.departureHubId(), route.destinationHubId());
			String key2 = key(route.destinationHubId(), route.departureHubId());
			EdgeWeight weight = new EdgeWeight(
				route.info().getDistance(),
				route.info().getInterval()
			);
			edgeWeights.put(key1, weight);
			edgeWeights.put(key2, weight);
		}
	}

	@Override
	public Set<UUID> hubIds() {
		return adjacency.keySet();
	}

	@Override
	public Map<UUID, Set<UUID>> adjacency() {
		return adjacency;
	}

	@Override
	public EdgeWeight getWeight(UUID a, UUID b) {
		return edgeWeights.get(key(a, b));
	}

	private String key(UUID a, UUID b) {
		return a + "-" + b;
	}
}