package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.List;
import java.util.UUID;

public record UpdateOrderCommand(
        Long userId,
        UUID orderId,
        Integer totalPrice,
        String requestMessage,
        List<OrderProductReq> orderProducts
) {
}
