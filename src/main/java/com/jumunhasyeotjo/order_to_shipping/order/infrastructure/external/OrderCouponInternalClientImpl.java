package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.UseCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCouponClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCouponInternalClientImpl implements OrderCouponClient {

    private final IssueCouponService issueCouponService;

    @Override
    public Integer useCoupon(UUID couponId, UUID orderId) {
        return issueCouponService.useCoupon(new UseCouponCommand(couponId, orderId));
    }
}
