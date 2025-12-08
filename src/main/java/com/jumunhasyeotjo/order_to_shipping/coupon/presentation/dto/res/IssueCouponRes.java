package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.result.IssueCouponResult;
import com.jumunhasyeotjo.order_to_shipping.coupon.domain.vo.IssueCouponStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueCouponRes(
    UUID issueId,
    UUID couponId,
    Long userId,
    IssueCouponStatus status,
    LocalDateTime createdAt
) {
    public static IssueCouponRes fromResult(IssueCouponResult result) {
        return new IssueCouponRes(
            result.issueId(),
            result.couponId(),
            result.userId(),
            result.status(),
            result.createdAt()
        );
    }
}
