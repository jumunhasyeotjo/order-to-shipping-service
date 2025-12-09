package com.jumunhasyeotjo.order_to_shipping.coupon.application.command;

import java.util.UUID;

public record IssueCouponCommand(
    UUID couponId,
    Long userId
) {
}
