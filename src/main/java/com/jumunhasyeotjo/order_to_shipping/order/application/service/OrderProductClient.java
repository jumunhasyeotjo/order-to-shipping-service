package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;

import java.util.List;
import java.util.UUID;

public interface OrderProductClient {
    List<ProductResult> findAllProducts(List<UUID> orderProducts);
}
