package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import static com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.RouteCacheKeys.field;
import static com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.RouteCacheKeys.redisHashKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jumunhasyeotjo.order_to_shipping.common.service.RedisService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShortestPathCacheTest {

	@Mock
	RedisService redisService;

	@InjectMocks
	ShortestPathCache cache;

	private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000A");
	private static final UUID D = UUID.fromString("00000000-0000-0000-0000-00000000000D");

	@Test
	@DisplayName("get: 캐시에 값이 있으면 Map으로 반환한다")
	void get_shouldReturnMap_Success() {
		// given
		WeightStrategy s = WeightStrategy.DISTANCE;
		String key = redisHashKey(s);
		String f = field(A, D);
		Map<String, Object> expected = Map.of("UNREACHABLE", false, "NODES", java.util.List.of(A.toString(), D.toString()));

		when(redisService.hashGetJson(eq(key), eq(f), any(TypeReference.class)))
			.thenReturn(expected);

		// when
		Map<String, Object> actual = cache.get(s, A, D);

		// then
		assertThat(actual).isEqualTo(expected);
		verify(redisService).hashGetJson(eq(key), eq(f), any(TypeReference.class));
		verifyNoMoreInteractions(redisService);
	}

	@Test
	@DisplayName("get: 캐시에 값이 없으면 null을 반환한다")
	void get_shouldReturnNull_Success() {
		// given
		WeightStrategy s = WeightStrategy.DISTANCE;
		String key = redisHashKey(s);
		String f = field(A, D);

		when(redisService.hashGetJson(eq(key), eq(f), any(TypeReference.class)))
			.thenReturn(null);

		// when
		Map<String, Object> actual = cache.get(s, A, D);

		// then
		assertThat(actual).isNull();
		verify(redisService).hashGetJson(eq(key), eq(f), any(TypeReference.class));
		verifyNoMoreInteractions(redisService);
	}

	@Test
	@DisplayName("put: 해시필드에 JSON 저장 후 키 TTL(일 단위)을 설정한다")
	void put_shouldStoreAndExpireDays_Success() {
		// given
		WeightStrategy s = WeightStrategy.DISTANCE;
		String key = redisHashKey(s);
		String f = field(A, D);
		Map<String, Object> dto = Map.of("UNREACHABLE", true);

		// when
		cache.put(s, A, D, dto);

		// then
		verify(redisService).hashPutJson(eq(key), eq(f), eq(dto));
		verify(redisService).expireDays(eq(key), eq(30L));
		verifyNoMoreInteractions(redisService);
	}

	@Test
	@DisplayName("deleteAll: 전략별 해시 키를 삭제한다(DISTANCE, DURATION)")
	void deleteAll_shouldDeleteBothStrategyKeys_Success() {
		// when
		cache.deleteAll();

		// then
		verify(redisService).delete(eq(redisHashKey(WeightStrategy.DISTANCE)));
		verify(redisService).delete(eq(redisHashKey(WeightStrategy.DURATION)));
		verifyNoMoreInteractions(redisService);
	}
}