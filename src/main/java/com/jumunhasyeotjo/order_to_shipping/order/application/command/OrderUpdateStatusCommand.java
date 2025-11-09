package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderUpdateStatusCommand(
        Long userId,
        UUID orderId,
        String role,
        OrderStatus status
) {
}
