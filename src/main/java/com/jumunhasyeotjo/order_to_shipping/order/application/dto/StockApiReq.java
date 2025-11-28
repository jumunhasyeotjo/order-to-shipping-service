package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;

import java.util.List;

public record StockApiReq (
        List<OrderProductReq> productList
) {
}
