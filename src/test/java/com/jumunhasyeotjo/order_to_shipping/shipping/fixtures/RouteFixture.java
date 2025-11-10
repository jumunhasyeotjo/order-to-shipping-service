package com.jumunhasyeotjo.order_to_shipping.shipping.fixtures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntBiFunction;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubDijkstraRouter;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubNetwork;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

public class RouteFixture {

	public static List<Route> createDefaultThreeNode(UUID A, UUID B, UUID C){
		RouteInfo routeInfo = RouteInfo.of(100, 100);

		List<Route> routes = List.of(
			Route.of(A, B, routeInfo),
			Route.of(B, C, routeInfo)
		);

		return routes;
	}

	public static final ToIntBiFunction<Integer, HubDijkstraRouter.Edge> DISTANCE =
		(idx, e) -> e.distance();

	public static final ToIntBiFunction<Integer, HubDijkstraRouter.Edge> INTERVAL =
		(idx, e) -> e.interval();

	/**
	 * 그래프:
	 * A - B (distance 5,  interval 10)
	 * B - C (distance 5,  interval 10)
	 * A - C (distance 20, interval 1)
	 * D (고립)
	 */
	public static HubNetwork buildNetwork(UUID A, UUID B, UUID C, UUID D) {
		Map<UUID, Set<UUID>> adjacency = new HashMap<>();
		adjacency.put(A, Set.of(B, C));
		adjacency.put(B, Set.of(A, C));
		adjacency.put(C, Set.of(A, B));
		adjacency.put(D, Set.of()); // 고립

		// 가중치 테이블 (양방향)
		HashMap<String, HubNetwork.EdgeWeight> weights = new HashMap<>();
		putW(weights, A, B, 5, 10);
		putW(weights, B, A, 5, 10);
		putW(weights, B, C, 5, 10);
		putW(weights, C, B, 5, 10);
		putW(weights, A, C, 20, 1);
		putW(weights, C, A, 20, 1);

		return new FakeNetwork(adjacency, weights);
	}

	private static void putW(Map<String, HubNetwork.EdgeWeight> map, UUID a, UUID b, int distance, int interval) {
		map.put(key(a, b), new HubNetwork.EdgeWeight(distance, interval));
	}

	private static String key(UUID a, UUID b) {
		return a.toString() + "-" + b.toString();
	}
}
