package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;

import java.util.UUID;

public record OrderProductResult(
        UUID orderProductId,
        String name,
        int price,
        int quantity
) {
    public static OrderProductResult of(OrderProduct orderProduct) {
        return new OrderProductResult(
                orderProduct.getProductId(),
                orderProduct.getName(),
                orderProduct.getPrice(),
                orderProduct.getQuantity()
        );
    }
}
