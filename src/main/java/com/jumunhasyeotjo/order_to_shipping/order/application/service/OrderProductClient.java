package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductListRes;

import java.util.List;
import java.util.UUID;

public interface OrderProductClient {
    @Metric(value = "주문 상품 조회 (외부)",level = Metric.Level.DEBUG)
    ProductListRes findAllProducts(List<UUID> orderProducts);
}
