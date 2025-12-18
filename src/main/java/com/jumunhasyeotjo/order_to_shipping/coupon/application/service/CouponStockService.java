package com.jumunhasyeotjo.order_to_shipping.coupon.application.service;

import java.util.UUID;

public interface CouponStockService {
    String key(UUID couponId);
    Long getStock(UUID couponId);
    Long decrease(UUID couponId);
    Long increase(UUID couponId);
    void setInitialStock(UUID couponId, Integer stock);

    String userSetKey(UUID couponId);
    Boolean exists(UUID couponId, Long userId);
    void issue(UUID couponId, Long userId);
}
