package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand (
        Long userId,
        UUID organizationId,
        String requestMessage,
        List<OrderProductReq> orderProducts,
        String idempotencyKey,
        UUID couponId,
        String tossPaymentKey,
        String tossOrderId
){
}
