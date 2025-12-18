package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;

import java.util.List;

public interface OrderStockClient {
    @Metric(value = "재고 조회 및 차감 (외부)",level = Metric.Level.DEBUG)
    ExternalExists decreaseStock(List<OrderProductReq> orderProducts, String orderId);
}
