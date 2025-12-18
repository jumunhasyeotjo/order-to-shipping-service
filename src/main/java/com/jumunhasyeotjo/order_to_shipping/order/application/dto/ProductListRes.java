package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import java.util.List;

public record ProductListRes(
        List<ProductResult> data
) {
}
