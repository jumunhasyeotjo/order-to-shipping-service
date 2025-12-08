package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.CouponCommand;

import java.time.LocalDate;

public record CouponReq(
    String couponName,
    Integer discountAmount,
    Integer maxQuantity,
    LocalDate validateStartDate,
    LocalDate validateEndDate
) {
    public CouponCommand toCommand() {
        return new CouponCommand(couponName, discountAmount, maxQuantity, validateStartDate, validateEndDate);
    }
}
