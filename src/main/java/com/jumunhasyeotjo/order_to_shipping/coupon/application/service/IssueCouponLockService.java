package com.jumunhasyeotjo.order_to_shipping.coupon.application.service;

import java.util.UUID;

public interface IssueCouponLockService {
    <T> T executeWithLock(UUID couponId, LockCallback<T> callback);

    interface LockCallback<T> {
        T call();
    }
}
