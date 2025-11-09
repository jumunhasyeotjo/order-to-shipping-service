package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.UUID;

public record GetOrderCommand(
        UUID orderId,
        Long userId,
        String role
) {
}
