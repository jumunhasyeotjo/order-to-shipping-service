package com.jumunhasyeotjo.order_to_shipping.coupon.application.result;

import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.IssueCoupon;
import com.jumunhasyeotjo.order_to_shipping.coupon.domain.vo.IssueCouponStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueCouponResult(
    UUID issueId,
    UUID couponId,
    Long userId,
    UUID orderId,
    IssueCouponStatus status,
    LocalDateTime createdAt
) {
    public static IssueCouponResult fromIssueCoupon(IssueCoupon issueCoupon) {
        return new IssueCouponResult(
            issueCoupon.getIssueId(),
            issueCoupon.getCoupon().getCouponId(),
            issueCoupon.getUserId(),
            issueCoupon.getOrderId(),
            issueCoupon.getStatus(),
            issueCoupon.getCreatedAt()
        );
    }
}
