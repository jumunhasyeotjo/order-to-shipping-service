package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import org.springframework.stereotype.Component;

import java.util.List;

public interface StockClient {
    boolean decreaseStock(List<OrderProductReq> orderProducts);
}
