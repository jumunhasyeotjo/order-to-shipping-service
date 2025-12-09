package com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository;


import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Coupon findById(UUID couponId);
    Page<Coupon> findAll(Pageable pageable);
}
