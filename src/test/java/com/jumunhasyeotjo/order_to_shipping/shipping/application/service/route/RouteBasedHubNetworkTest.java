package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.RouteFixture;

class RouteBasedHubNetworkTest {

	private static final UUID A = UUID.randomUUID();
	private static final UUID B = UUID.randomUUID();
	private static final UUID C = UUID.randomUUID();

	@Test
	@DisplayName("hubIds()에는 라우트에 등장하는 모든 허브 ID가 포함된다")
	void hubIds_shouldContainAllUniqueHubs() {
		// given
		List<Route> routes = RouteFixture.createDefaultThreeNode(A,B,C);

		// when
		RouteBasedHubNetwork network = new RouteBasedHubNetwork(routes);

		// then
		assertThat(network.hubIds())
			.containsExactlyInAnyOrder(A, B, C);
	}

	@Test
	@DisplayName("인접 리스트는 양방향으로 구성된다")
	void adjacency_shouldBeBidirectional() {
		// given
		List<Route> routes = RouteFixture.createDefaultThreeNode(A,B,C);

		// when
		RouteBasedHubNetwork network = new RouteBasedHubNetwork(routes);

		// then
		Map<UUID, Set<UUID>> adj = network.adjacency();
		assertThat(adj.get(A)).containsExactlyInAnyOrder(B);
		assertThat(adj.get(B)).containsExactlyInAnyOrder(A, C);
		assertThat(adj.get(C)).containsExactlyInAnyOrder(B);

		assertThat(adj.keySet()).isEqualTo(network.hubIds());
	}

	@Test
	@DisplayName("가중치는 양방향 동일하게 조회된다(거리/시간)")
	void weight_shouldReturnEdgeWeightForBothDirections() {
		// given
		UUID A = UUID.randomUUID();
		UUID B = UUID.randomUUID();
		int distance = 42;
		int interval = 13;
		RouteInfo routeInfo = RouteInfo.of(distance, interval);

		List<Route> routes = List.of(Route.of(A, B, routeInfo));

		// when
		RouteBasedHubNetwork network = new RouteBasedHubNetwork(routes);

		// then
		HubNetwork.EdgeWeight w1 = network.getWeight(A, B);
		HubNetwork.EdgeWeight w2 = network.getWeight(B, A);

		assertThat(w1).isNotNull();
		assertThat(w2).isNotNull();

		assertThat(w1.distance()).isEqualTo(distance);
		assertThat(w1.interval()).isEqualTo(interval);

		assertThat(w2.distance()).isEqualTo(distance);
		assertThat(w2.interval()).isEqualTo(interval);
	}

	@Test
	@DisplayName("직접 연결이 없으면 가중치는 null을 반환한다")
	void weight_shouldReturnNullWhenNoDirectEdge() {
		// given
		UUID D = UUID.randomUUID();
		List<Route> routes = RouteFixture.createDefaultThreeNode(A,B,C);


		// when
		RouteBasedHubNetwork network = new RouteBasedHubNetwork(routes);

		// then
		assertThat(network.getWeight(A, D)).isNull();
		assertThat(network.getWeight(D, A)).isNull();
	}

}