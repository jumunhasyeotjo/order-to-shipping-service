package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;

import java.util.UUID;

public interface OrderCouponClient {
    @Metric(value = "쿠폰 사용 (내부)", level = Metric.Level.DEBUG)
    Integer useCoupon(UUID couponId, UUID orderId);
}
