package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.IssueCouponCommand;

import java.util.UUID;

public record IssueCouponReq(
    UUID couponId,
    Long userId
) {
    public IssueCouponCommand toCommand() {
        return new IssueCouponCommand(couponId, userId);
    }
}
