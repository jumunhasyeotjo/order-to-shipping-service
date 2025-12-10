package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.UUID;

public interface OrderCouponClient {
    Integer useCoupon(UUID couponId, UUID orderId);
}
