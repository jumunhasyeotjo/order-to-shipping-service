package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;

import java.util.List;

public interface StockClient {
    boolean decreaseStock(List<OrderProductReq> orderProducts);
    void restoreStocks(List<OrderProduct> orderProducts);
}
