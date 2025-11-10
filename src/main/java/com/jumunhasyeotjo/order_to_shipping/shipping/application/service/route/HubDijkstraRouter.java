package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntBiFunction;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;

/**
 * 허브 네트워크(HubNetwork)를 기반으로 그래프를 구성하고,
 * 다익스트라(Dijkstra) 알고리즘을 이용해 두 허브 간 최단 경로를 계산하는 클래스
 */
public class HubDijkstraRouter {
	public record Edge(UUID to, int distance, int interval) {
	}

	private final Map<UUID, List<Edge>> graph = new HashMap<>();

	public record Path(List<UUID> nodes, long cost) {
	}

	/**
	 * network 정보로 그래프 그리기
	 */
	public HubDijkstraRouter(HubNetwork provider) {
		provider.adjacency().forEach((a, neighbors) -> {
			neighbors.forEach(b -> {
				HubNetwork.EdgeWeight w = provider.weight(a, b);
				graph.computeIfAbsent(a, k -> new ArrayList<>()).add(new Edge(b, w.distance(), w.interval()));
				graph.computeIfAbsent(b, k -> new ArrayList<>()).add(new Edge(a, w.distance(), w.interval()));
			});
		});
	}

	/**
	 * 최단 경로 계산
	 * @param departureHubId 출발허브
	 * @param destinationHubId 도착허브
	 * @param weightFn 가중치 계산 함수
	 * @return 최단 경로
	 */
	public Path shortest(UUID departureHubId, UUID destinationHubId, ToIntBiFunction<Integer, Edge> weightFn) {
		Map<UUID, Long> dist = new HashMap<>();
		Map<UUID, UUID> prev = new HashMap<>();
		Set<UUID> visited = new HashSet<>();
		graph.keySet().forEach(h -> dist.put(h, Long.MAX_VALUE));
		dist.put(departureHubId, 0L);
		PriorityQueue<Map.Entry<UUID, Long>> pq = new PriorityQueue<>(Comparator.comparingLong(Map.Entry::getValue));
		pq.add(Map.entry(departureHubId, 0L));

		while (!pq.isEmpty()) {
			var cur = pq.poll();
			UUID u = cur.getKey();
			if (!visited.add(u))
				continue;
			if (u.equals(destinationHubId))
				break;
			List<Edge> edges = graph.getOrDefault(u, List.of());
			for (int i = 0; i < edges.size(); i++) {
				Edge e = edges.get(i);
				if (visited.contains(e.to))
					continue;
				long w = Math.max(0, weightFn.applyAsInt(i, e));
				long alt = dist.get(u) + w;
				if (alt < dist.get(e.to)) {
					dist.put(e.to, alt);
					prev.put(e.to, u);
					pq.add(Map.entry(e.to, alt));
				}
			}
		}

		LinkedList<UUID> path = reconstructPath(prev, departureHubId, destinationHubId);

		return new Path(
			path,
			dist.getOrDefault(destinationHubId, 0L)
		);
	}

	/**
	 * prev 맵을 거꾸로 따라가면서 실제 경로 리스트를 만듬
	 */
	private LinkedList<UUID> reconstructPath(Map<UUID, UUID> prev, UUID departureHubId, UUID destinationHubId) {
		validateConnectionExists(prev, departureHubId, destinationHubId);

		LinkedList<UUID> path = new LinkedList<>();
		UUID cur = destinationHubId;
		path.addFirst(cur);
		while (!cur.equals(departureHubId)) {
			cur = prev.get(cur);
			if (cur == null)
				break;
			path.addFirst(cur);
		}
		return path;
	}

	private void validateConnectionExists(Map<UUID, UUID> prev, UUID departureHubId, UUID destinationHubId) {
		if (!departureHubId.equals(destinationHubId) && !prev.containsKey(destinationHubId)) {
			throw new BusinessException(ErrorCode.CONNECTION_NOT_FOUND_BETWEEN_HUBS);
		}
	}
}