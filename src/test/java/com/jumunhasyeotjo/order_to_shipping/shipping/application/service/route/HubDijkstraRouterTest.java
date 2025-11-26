package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.ToIntBiFunction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.RouteFixture;

/**
 * HubDijkstraRouter 단위 테스트
 */
class HubDijkstraRouterTest {

	private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000A");
	private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000B");
	private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000C");
	private static final UUID D = UUID.fromString("00000000-0000-0000-0000-00000000000D");


	private HubNetwork buildNetwork() {
		return RouteFixture.buildNetwork(A, B, C, D);
	}

	@Test
	@DisplayName("거리 가중치 기준: A→C 최단 경로는 A-B-C (총 10)")
	void shortestByDistance_Success() {
		// given
		HubNetwork hubNetwork = buildNetwork();
		HubDijkstraRouter router = new HubDijkstraRouter(hubNetwork);

		// when
		HubDijkstraRouter.Path path = router.shortest(A, C, RouteFixture.DISTANCE);

		// then
		assertThat(path.nodes()).containsExactly(A, B, C);
		assertThat(path.cost()).isEqualTo(10);
	}

	@Test
	@DisplayName("소요시간(인터벌) 가중치 기준: A→C 최단 경로는 A-C (총 1)")
	void shortestByInterval_Success() {
		//given
		HubDijkstraRouter router = new HubDijkstraRouter(buildNetwork());

		//when
		HubDijkstraRouter.Path path = router.shortest(A, C, RouteFixture.INTERVAL);

		//then
		assertThat(path.nodes()).containsExactly(A, C);
		assertThat(path.cost()).isEqualTo(1);
	}

	@Test
	@DisplayName("출발지와 도착지가 같으면 자기 자신만 포함한 경로와 0 비용을 반환한다")
	void sameSourceAndDestination_Return0() {
		//given
		HubDijkstraRouter router = new HubDijkstraRouter(buildNetwork());

		//when
		HubDijkstraRouter.Path path = router.shortest(A, A, RouteFixture.DISTANCE);

		//then
		assertThat(path.nodes()).containsExactly(A);
		assertThat(path.cost()).isEqualTo(0L);
	}

	@Test
	@DisplayName("연결 불가: A→D 경로가 없으면 BusinessException을 던진다")
	void unreachable_ShouldThrow_BusinessException() {
		//given
		HubDijkstraRouter router = new HubDijkstraRouter(buildNetwork());

		//when&then
		assertThatThrownBy(() -> router.shortest(A, D, RouteFixture.DISTANCE))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("음수 가중치 방어: 음수 결과가 나오더라도 0으로 보정된다")
	void negativeWeightIsClampedToZero_Success() {
		//given
		HubDijkstraRouter router = new HubDijkstraRouter(buildNetwork());

		// 모든 간선 가중치를 -5로 반환하도록 하여, 내부의 Math.max(0, ..) 동작을 검증
		ToIntBiFunction<Integer, HubDijkstraRouter.Edge> NEGATIVE = (i, e) -> -5;

		// when
		HubDijkstraRouter.Path path = router.shortest(A, C, NEGATIVE);

		// then
		assertThat(path.cost()).isEqualTo(0L);
		// 경로 자체는 존재하는 최단 경로 중 하나여야 함 (A-C 또는 A-B-C 모두 0 비용이므로 A-C일 가능성 높음)
		assertThat(path.nodes().get(0)).isEqualTo(A);
		assertThat(path.nodes().get(path.nodes().size() - 1)).isEqualTo(C);
	}
}