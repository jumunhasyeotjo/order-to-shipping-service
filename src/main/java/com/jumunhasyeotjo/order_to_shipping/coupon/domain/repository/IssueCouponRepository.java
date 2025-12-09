package com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.IssueCoupon;

import java.util.UUID;

public interface IssueCouponRepository {
    IssueCoupon save(IssueCoupon issueCoupon);
    IssueCoupon findById(UUID issueCouponId);
    boolean existsByCoupon_CouponIdAndUserId(UUID couponId, Long userId);
}
