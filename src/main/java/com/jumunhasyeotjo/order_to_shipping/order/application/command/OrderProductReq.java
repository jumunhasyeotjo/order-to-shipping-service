package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.UUID;

public record OrderProductReq(
        UUID productId,
        Integer quantity
) {
}
