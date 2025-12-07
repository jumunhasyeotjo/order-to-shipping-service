package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.UUID;

public interface OrderCouponClient {
    boolean useCoupon(UUID couponId, UUID orderId);
}
