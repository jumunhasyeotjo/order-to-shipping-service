package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductListRes;

import java.util.List;
import java.util.UUID;

public interface OrderProductClient {
    ProductListRes findAllProducts(List<UUID> orderProducts);
}
