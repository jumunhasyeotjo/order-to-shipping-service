package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface HubNetwork {
	Set<UUID> hubIds();

	/** 인접 리스트 (양방향 간선) */
	Map<UUID, Set<UUID>> adjacency();

	/** 가중치 */
	default EdgeWeight getWeight(UUID a, UUID b) {
		return new EdgeWeight(0, 0);
	}

	record EdgeWeight(int distance, int interval) {
	}
}
