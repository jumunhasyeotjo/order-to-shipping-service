package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCouponClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderCouponClientImpl implements OrderCouponClient {

    @Override
    public boolean useCoupon(UUID couponId, UUID orderId) {
        return true;
    }
}
