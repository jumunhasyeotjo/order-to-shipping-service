package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID orderId,
        String requestMessage,
        int totalPrice,
        OrderStatus status,
        List<OrderProductResult> orderProducts
) {
    public static OrderResult of(Order order) {
        return new OrderResult(
                order.getId(),
                order.getRequestMessage(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderProducts().stream().map(OrderProductResult::of).toList()
        );
    }
}
