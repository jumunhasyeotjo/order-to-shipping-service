package com.jumunhasyeotjo.order_to_shipping.coupon.application.event;

import java.util.UUID;

public record CouponIssueEvent(
    UUID couponId,
    Long userId
) {
}
