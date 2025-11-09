package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import java.util.UUID;

public record ProductResult(
        UUID productId,
        String name,
        int price,
        int quantity
) {
}
