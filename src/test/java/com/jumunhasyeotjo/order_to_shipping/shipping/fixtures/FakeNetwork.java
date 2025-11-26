package com.jumunhasyeotjo.order_to_shipping.shipping.fixtures;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubNetwork;

/**
 * 간단한 Fake HubNetwork 구현
 */
class FakeNetwork implements HubNetwork {
	private final Map<UUID, Set<UUID>> adjacency;
	private final Map<String, EdgeWeight> weights;

	FakeNetwork(Map<UUID, Set<UUID>> adjacency, Map<String, EdgeWeight> weights) {
		this.adjacency = adjacency;
		this.weights = weights;
	}

	@Override
	public Map<UUID, Set<UUID>> adjacency() {
		return adjacency;
	}

	@Override
	public Set<UUID> hubIds() {
		return adjacency.keySet();
	}

	@Override
	public EdgeWeight getWeight(UUID a, UUID b) {
		return weights.get(key(a, b)); // 테스트에서는 존재하는 간선만 요청됨
	}


	private static String key(UUID a, UUID b) {
		return a.toString() + "-" + b.toString();
	}


}