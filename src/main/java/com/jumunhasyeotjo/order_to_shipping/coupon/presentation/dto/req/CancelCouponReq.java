package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.CancelCouponCommand;

import java.util.UUID;

public record CancelCouponReq(
    UUID issueCouponId,
    UUID orderId
) {
    public CancelCouponCommand toCommand() {
        return new CancelCouponCommand(issueCouponId, orderId);
    }
}
