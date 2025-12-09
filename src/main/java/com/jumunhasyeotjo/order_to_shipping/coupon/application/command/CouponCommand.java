package com.jumunhasyeotjo.order_to_shipping.coupon.application.command;

import java.time.LocalDate;

public record CouponCommand(
    String couponName,
    Integer discountAmount,
    Integer maxQuantity,
    LocalDate validateStartDate,
    LocalDate validateEndDate
) {
}
