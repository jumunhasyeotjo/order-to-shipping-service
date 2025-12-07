package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderStockClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderStockClientImpl implements OrderStockClient {

    @Override
    public boolean decreaseStock(List<OrderProductReq> orderProducts, UUID orderId) {
        return true;
    }
}
