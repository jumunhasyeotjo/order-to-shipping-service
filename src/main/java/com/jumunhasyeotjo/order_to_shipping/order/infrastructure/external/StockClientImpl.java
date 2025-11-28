package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockClientImpl implements StockClient {

    @Override
    public boolean decreaseStock(List<OrderProductReq> orderProducts, String idempotentKey) {
        return true;
    }

    @Override
    public void restoreStocks(List<OrderProductReq> orderProducts, String idempotentKey) {

    }
}
