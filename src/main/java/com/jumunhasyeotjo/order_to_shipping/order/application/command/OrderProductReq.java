package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.UUID;

import org.aspectj.weaver.ast.Or;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;

public record OrderProductReq(
        UUID productId,
        Integer quantity
) {
}
