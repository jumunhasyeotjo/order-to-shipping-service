package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.dto;

import java.util.UUID;

public record OrderRollbackEvent(
    UUID issueCouponId,
    UUID orderId
) {
}
