package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.RouteFixture;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.RouteCacheKeys;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.ShortestPathCache;

@ExtendWith(MockitoExtension.class)
class DijkstraRouterFacadeTest {

	private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000A");
	private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000B");
	private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000C");
	private static final UUID D = UUID.fromString("00000000-0000-0000-0000-00000000000D");


	private HubNetwork buildNetwork() {
		return RouteFixture.buildNetwork(A, B, C, D);
	}

	// 캐시 put에 들어간 값 꺼내기
	private static List<UUID> readCachedNodes(Map<String, Object> map) {
		List<String> nodeStr = (List<String>) map.get(RouteCacheKeys.NODES_KEY);
		return nodeStr == null ? List.of() : nodeStr.stream().map(UUID::fromString).collect(Collectors.toList());
	}

	@Nested
	@DisplayName("캐시 없음 - 다익스트라 수행")
	class CacheMiss {

		@Test
		@DisplayName("A→C 최단 경로는 A-B-C(거리 기준) 이며, 캐시에 저장된다")
		void computeAndCache_Success() {
			//given
			ShortestPathCache cache = mock(ShortestPathCache.class);
			when(cache.get(eq(WeightStrategy.DISTANCE), eq(A), eq(C))).thenReturn(null);

			HubNetwork network = buildNetwork();
			DijkstraRouterFacade facade = new DijkstraRouterFacade(network, cache);

			// when
			List<Route> routes = facade.generateOrRebuildRoute(A, C);

			// 결과 검증: 거리 기준으로 A-B(5) + B-C(5) = 10 이므로 A-B-C가 최단경로
			// then
			assertThat(routes).hasSize(2);
			assertThat(routes.get(0).departureHubId()).isEqualTo(A);
			assertThat(routes.get(0).destinationHubId()).isEqualTo(B);
			assertThat(routes.get(0).info()).isEqualTo(RouteInfo.of(5, 10));
			assertThat(routes.get(1).departureHubId()).isEqualTo(B);
			assertThat(routes.get(1).destinationHubId()).isEqualTo(C);
			assertThat(routes.get(1).info()).isEqualTo(RouteInfo.of(5, 10));

			// 캐시 저장 내용 검증
			ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
			verify(cache).put(eq(WeightStrategy.DISTANCE), eq(A), eq(C), mapCaptor.capture());
			Map<String, Object> saved = mapCaptor.getValue();
			assertThat(saved.get(RouteCacheKeys.UNREACHABLE_KEY)).isEqualTo(false);
			assertThat(readCachedNodes(saved)).containsExactly(A, B, C);
		}

		@Test
		@DisplayName("경로가 없으면 CONNECTION_NOT_FOUND_BETWEEN_HUBS 예외를 던진다.")
		void noPath_ThenCacheUnreachable_ThrowException() {
			//given
			ShortestPathCache cache = mock(ShortestPathCache.class);
			when(cache.get(eq(WeightStrategy.DISTANCE), eq(A), eq(D))).thenReturn(null);

			HubNetwork network = buildNetwork();
			DijkstraRouterFacade facade = new DijkstraRouterFacade(network, cache);

			//when&then
			assertThatThrownBy(() -> facade.generateOrRebuildRoute(A, D))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONNECTION_NOT_FOUND_BETWEEN_HUBS);
		}
	}

	@Nested
	@DisplayName("캐시 히트")
	class CacheHit {

		@Test
		@DisplayName("UNREACHABLE=true 이면 예외를 던진다")
		void hitUnreachable_throwException() {
			//given
			ShortestPathCache cache = mock(ShortestPathCache.class);
			Map<String, Object> cached = Map.of(RouteCacheKeys.UNREACHABLE_KEY, true);
			when(cache.get(eq(WeightStrategy.DISTANCE), eq(A), eq(C))).thenReturn(cached);

			HubNetwork network = buildNetwork();
			DijkstraRouterFacade facade = new DijkstraRouterFacade(network, cache);

			//when&then
			assertThatThrownBy(() -> facade.generateOrRebuildRoute(A, C))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONNECTION_NOT_FOUND_BETWEEN_HUBS);

			verify(cache, never()).put(any(), any(), any(), any());
		}

		@Test
		@DisplayName("NODES가 캐시에 있으면 그대로 경로를 만들고 캐시에 새로 쓰지 않는다")
		void hitNodesRebuildRoutesOnly() {
			//given
			ShortestPathCache cache = mock(ShortestPathCache.class);
			List<String> nodesStr = List.of(A.toString(), C.toString());
			Map<String, Object> cached = Map.of(
				RouteCacheKeys.UNREACHABLE_KEY, false,
				RouteCacheKeys.NODES_KEY, nodesStr
			);
			when(cache.get(eq(WeightStrategy.DISTANCE), eq(A), eq(C))).thenReturn(cached);

			HubNetwork network = buildNetwork();
			DijkstraRouterFacade facade = new DijkstraRouterFacade(network, cache);

			//when
			List<Route> routes = facade.generateOrRebuildRoute(A, C);

			//then
			assertThat(routes).hasSize(1);
			Route r = routes.get(0);
			assertThat(r.departureHubId()).isEqualTo(A);
			assertThat(r.destinationHubId()).isEqualTo(C);
			assertThat(r.info()).isEqualTo(RouteInfo.of(20, 1));

			verify(cache, never()).put(any(), any(), any(), any());
		}
	}
}