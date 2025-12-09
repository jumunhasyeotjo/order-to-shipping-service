package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.UseCouponCommand;

import java.util.UUID;

public record UseCouponReq(
    UUID issueCouponId,
    UUID orderId
) {
    public UseCouponCommand toCommand() {
        return new UseCouponCommand(issueCouponId, orderId);
    }
}
