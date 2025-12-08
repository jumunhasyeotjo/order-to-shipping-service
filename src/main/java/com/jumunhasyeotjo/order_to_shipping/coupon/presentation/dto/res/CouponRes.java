package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.result.CouponResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponRes(
    UUID couponId,
    String couponName,
    Integer discountAmount,
    Integer maxQuantity,
    Integer issuedQuantity,
    LocalDate validateStartDate,
    LocalDate validateEndDate,
    LocalDateTime createdAt
) {
    public static CouponRes fromResult(CouponResult result) {
        return new CouponRes(
            result.couponId(),
            result.couponName(),
            result.discountAmount(),
            result.maxQuantity(),
            result.issuedQuantity(),
            result.validateStartDate(),
            result.validateEndDate(),
            result.createdAt()
        );
    }
}
