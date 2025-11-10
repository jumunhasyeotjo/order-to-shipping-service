package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.DijkstraRouterFacade;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubNetwork;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubDijkstraRouter;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortestPathPreBuilder {
	private final DijkstraRouterFacade dijkstraRouterFacade;
	private final HubNetwork network;
	private final ShortestPathCache cache;

	public void rebuildAllPairs(WeightStrategy strategy) {
		List<UUID> hubs = new ArrayList<>(network.hubIds());
		HubDijkstraRouter router = new HubDijkstraRouter(network);

		for (UUID origin : hubs) {
			for (UUID dest : hubs) {
				if (origin.equals(dest))
					continue;
				dijkstraRouterFacade.buildShortestRouteForPair(strategy, origin, dest, router);
			}
		}
	}

	public void rebuildAllPairsForBoth() {
		rebuildAllPairs(WeightStrategy.DISTANCE);
		rebuildAllPairs(WeightStrategy.DURATION);
	}

	public void invalidateAll() {
		cache.deleteAll();
	}

}