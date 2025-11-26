package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;

import java.util.List;

public interface ProductClient {
    List<ProductResult> findAllProducts(List<OrderProductReq> orderProducts);
}
