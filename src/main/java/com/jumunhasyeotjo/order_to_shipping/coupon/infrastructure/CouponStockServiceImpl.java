package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;


import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.CouponStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponStockServiceImpl implements CouponStockService {

    private final StringRedisTemplate couponRedisTemplate;

    private static final String STOCK_KEY = "coupon:stock:%s";
    private static final String USER_SET_KEY = "coupon:issued:%s";

    /*
    * 여기는 Redis 에서 재고를 관리하기 위한 자료 구조다.
     */
    public String key(UUID couponId) {
        return String.format(STOCK_KEY, couponId);
    }

    public Long getStock(UUID couponId) {
        String val = couponRedisTemplate.opsForValue().get(key(couponId));
        return val == null ? 0 : Long.parseLong(val);
    }

    public Long decrease(UUID couponId) {
        return couponRedisTemplate.opsForValue().decrement(key(couponId));
    }

    public Long increase(UUID couponId) {
        return couponRedisTemplate.opsForValue().increment(key(couponId));
    }

    public void setInitialStock(UUID couponId, Integer stock) {
        couponRedisTemplate.opsForValue().set(key(couponId), String.valueOf(stock));
    }

    /*
     * 여기는 Redis 에서 쿠폰별 중복 유저를 체크하기 위한 자료구조다.
     */
    public String userSetKey(UUID couponId) {
        return String.format(USER_SET_KEY, couponId);
    }

    public Boolean exists(UUID couponId, Long userId) {
        String userSetKey = userSetKey(couponId);
        return couponRedisTemplate.opsForSet().isMember(userSetKey, userId.toString());
    }

    public void issue(UUID couponId, Long userId) {
        String userSetKey = userSetKey(couponId);
        couponRedisTemplate.opsForSet().add(userSetKey, userId.toString());
    }
}
