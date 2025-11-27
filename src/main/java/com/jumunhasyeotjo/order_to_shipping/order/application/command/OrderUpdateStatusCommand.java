package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;

import java.util.UUID;

public record OrderUpdateStatusCommand(
        Long userId,
        UUID organizationId,
        UUID orderId,
        String role,
        OrderStatus status
) {
}
