package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository;


import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.Coupon;
import com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponRepository jpaCouponRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaCouponRepository.save(coupon);
    }

    @Override
    public Coupon findById(UUID couponId) {
        return jpaCouponRepository.findById(couponId).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return null;
    }
}
