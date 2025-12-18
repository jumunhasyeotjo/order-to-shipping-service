package com.jumunhasyeotjo.order_to_shipping.coupon.application.result;

import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.Coupon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResult(
    UUID couponId,
    String couponName,
    Integer discountAmount,
    Integer maxQuantity,
    Integer issuedQuantity,
    LocalDate validateStartDate,
    LocalDate validateEndDate,
    LocalDateTime createdAt
) {
    public static CouponResult fromCoupon(Coupon coupon) {
        return new CouponResult(
            coupon.getCouponId(),
            coupon.getCouponName(),
            coupon.getDiscountAmount(),
            coupon.getMaxQuantity(),
            coupon.getIssuedQuantity(),
            coupon.getValidateStartDate(),
            coupon.getValidateEndDate(),
            coupon.getCreatedAt()
        );
    }
}
