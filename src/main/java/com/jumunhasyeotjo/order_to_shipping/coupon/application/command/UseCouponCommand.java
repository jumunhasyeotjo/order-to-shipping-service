package com.jumunhasyeotjo.order_to_shipping.coupon.application.command;

import java.util.UUID;

public record UseCouponCommand(
    UUID issueCouponId,
    UUID orderId
) {
}
