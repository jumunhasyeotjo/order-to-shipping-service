package com.jumunhasyeotjo.order_to_shipping.coupon.application;


import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.CouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.result.CouponResult;
import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.Coupon;
import com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public CouponResult getCoupon(UUID couponId) {
        return CouponResult.fromCoupon(couponRepository.findById(couponId));
    }

    @Transactional
    public CouponResult createCoupon(CouponCommand command) {
        Coupon coupon = Coupon.createCoupon(
            command.couponName(),
            command.discountAmount(),
            command.maxQuantity(),
            command.validateStartDate(),
            command.validateEndDate()
        );

        return CouponResult.fromCoupon(couponRepository.save(coupon));
    }
}
