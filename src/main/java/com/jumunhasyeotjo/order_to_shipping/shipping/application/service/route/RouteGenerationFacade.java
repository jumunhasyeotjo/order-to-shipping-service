package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.ShortestPathCache;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.key.RouteCacheKeys;

import lombok.RequiredArgsConstructor;

/**
 * 최단 경로 생성 Facade
 */
@Component
@RequiredArgsConstructor
public class RouteGenerationFacade implements ShippingRouteGenerator {
	private final HubNetwork network;
	private final ShortestPathCache cache;
	private final HubClient hubClient;
	private final WeightStrategy weightStrategy = WeightStrategy.DISTANCE; // default 가중치 전략

	/**
	 * 캐시에서 조회 후 있으면 캐시값 반환
	 * 없으면 최단 경로 생성
	 */
	@Override
	public List<Route> generateOrRebuildRoute(UUID originHubId, UUID arrivalHubId) {
		Map<String, Object> cached = cache.get(weightStrategy, originHubId, arrivalHubId);
		if (cached == null) {
			// 캐시 미스 시, 최신 라우트로 그래프를 재구성하여 사용
			HubDijkstraRouter router = generateNewRouter();
			return buildShortestRouteForPair(weightStrategy, originHubId, arrivalHubId, router);
		} else {
			validateReachable(cached);
			List<String> nodeString = (List<String>)cached.get(RouteCacheKeys.NODES_KEY);
			List<UUID> nodes = nodeString.stream().map(UUID::fromString).toList();
			return toRoutesFromNodes(nodes);
		}
	}

	private void validateReachable(Map<String, Object> cached) {
		if (Boolean.TRUE.equals(cached.getOrDefault(RouteCacheKeys.UNREACHABLE_KEY, false))) {
			throw new BusinessException(ErrorCode.CONNECTION_NOT_FOUND_BETWEEN_HUBS);
		}
	}

	public List<Route> buildShortestRouteForPair(WeightStrategy weightStrategy, UUID originHubId, UUID arrivalHubId,
		HubDijkstraRouter dijkstra) {
		HubDijkstraRouter.Path path = switch (weightStrategy) {
			case DISTANCE -> dijkstra.shortest(originHubId, arrivalHubId, (i, edge) -> edge.distance());
			case DURATION -> dijkstra.shortest(originHubId, arrivalHubId, (i, edge) -> edge.interval());
		};
		List<UUID> nodes = path.nodes();

		if (nodes.size() < 2) {
			cache.put(weightStrategy, originHubId, arrivalHubId, Map.of(RouteCacheKeys.UNREACHABLE_KEY, true));
			throw new BusinessException(ErrorCode.CONNECTION_NOT_FOUND_BETWEEN_HUBS);
		} else {
			cache.put(weightStrategy, originHubId, arrivalHubId,
				Map.of(RouteCacheKeys.UNREACHABLE_KEY, false, RouteCacheKeys.NODES_KEY,
					nodes.stream().map(UUID::toString).toList()));
			return toRoutesFromNodes(nodes);
		}
	}

	private HubDijkstraRouter generateNewRouter(){
		List<Route> freshRoutes = hubClient.getRoutes();
		HubNetwork freshNetwork = new RouteBasedHubNetwork(freshRoutes);
		return new HubDijkstraRouter(freshNetwork);
	}

	private List<Route> toRoutesFromNodes(List<UUID> nodes) {
		return IntStream.range(0, nodes.size() - 1)
			.mapToObj(i -> {
				UUID a = nodes.get(i);
				UUID b = nodes.get(i + 1);
				var w = network.getWeight(a, b);
				return new Route(a, b, RouteInfo.of(w.distance(), w.interval()));
			})
			.toList();
	}

}