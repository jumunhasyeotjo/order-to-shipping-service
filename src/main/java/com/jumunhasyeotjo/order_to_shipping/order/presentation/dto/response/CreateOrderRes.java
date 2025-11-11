package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;

import java.util.UUID;

public record CreateOrderRes(
        UUID orderId,
        OrderStatus status
) {
}
