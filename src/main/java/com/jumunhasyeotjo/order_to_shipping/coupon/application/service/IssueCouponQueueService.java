package com.jumunhasyeotjo.order_to_shipping.coupon.application.service;

import java.util.UUID;

public interface IssueCouponQueueService {
    String key(UUID couponId);
    Long joinQueue(UUID couponId, Long userId);
    Long getUserPosition(UUID couponId, Long userId);
    Long popFirstUser(UUID couponId);
}