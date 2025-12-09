package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.CancelReason;

import java.util.UUID;

public record CancelOrderCommand(
        Long userId,
        UUID organizationId,
        UUID orderId,
        String role,
        CancelReason reason
) {
}
