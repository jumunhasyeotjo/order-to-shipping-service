package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.UUID;

public record CancelOrderCommand(
        Long userId,
        UUID orderId,
        String role
) {
}
