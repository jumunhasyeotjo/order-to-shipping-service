package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;

import java.util.List;
import java.util.UUID;

public interface OrderStockClient {
    boolean decreaseStock(List<OrderProductReq> orderProducts, UUID orderId);
}
