package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.IssueCouponQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * 선착순 쿠폰 발급을 위한 Redis 기반 대기열 서비스.
 * - 사용자 발급 요청 → 대기열에 등록
 * - 정해진 순번에 도달한 사용자만 쿠폰 발급 가능
 */
@Service
@RequiredArgsConstructor
public class IssueCouponQueueServiceImpl implements IssueCouponQueueService {

    private final StringRedisTemplate couponRedisTemplate;

    private static final String QUEUE_KEY = "coupon:queue:%s";

    public String key(UUID couponId) {
        return String.format(QUEUE_KEY, couponId);
    }

    public Long joinQueue(UUID couponId, Long userId) {
        String key = key(couponId);
        String ttlKey = key + ":ttl";

        Boolean created = couponRedisTemplate.opsForValue()
            .setIfAbsent(ttlKey, "1", Duration.ofMinutes(120));

        if (Boolean.TRUE.equals(created)) {
            couponRedisTemplate.expire(key, Duration.ofMinutes(120));
        }

        long score = System.currentTimeMillis();
        couponRedisTemplate.opsForZSet().add(key, userId.toString(), score);

        return getUserPosition(couponId, userId);
    }

    public Long getUserPosition(UUID couponId, Long userId) {
        String k = key(couponId);

        Long rank = couponRedisTemplate.opsForZSet().rank(k, userId.toString());
        if (rank == null) return -1L;

        return rank + 1;
    }

    public Long popFirstUser(UUID couponId) {
        String k = key(couponId);

        Set<String> result = couponRedisTemplate.opsForZSet().range(k, 0, 0);
        if (result == null || result.isEmpty()) return null;

        String userId = result.iterator().next();
        couponRedisTemplate.opsForZSet().remove(k, userId);

        return Long.valueOf(userId);
    }
}