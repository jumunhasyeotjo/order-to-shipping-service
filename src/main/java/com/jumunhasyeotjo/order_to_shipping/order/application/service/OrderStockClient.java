package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;

import java.util.List;

public interface OrderStockClient {
    ExternalExists decreaseStock(List<OrderProductReq> orderProducts, String orderId);
}
