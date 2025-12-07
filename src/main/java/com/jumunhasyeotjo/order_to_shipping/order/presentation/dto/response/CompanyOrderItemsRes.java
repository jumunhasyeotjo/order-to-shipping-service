package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response;

import java.util.UUID;

public record CompanyOrderItemsRes(
        UUID productId,
        Integer quantity
) {
}
