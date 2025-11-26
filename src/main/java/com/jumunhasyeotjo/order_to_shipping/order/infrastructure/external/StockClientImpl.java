package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockClientImpl implements StockClient {
    @Override
    public boolean decreaseStock(List<OrderProductReq> orderProducts) {
        return true;
    }

    @Override
    public void restoreStocks(List<OrderProduct> orderProducts) {

    }
}
