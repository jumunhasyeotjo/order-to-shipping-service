package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.library.passport.entity.ApiRes;

import java.util.List;
import java.util.UUID;

public interface OrderStockClient {
    ApiRes<Boolean> decreaseStock(List<OrderProductReq> orderProducts, UUID orderId);
}
