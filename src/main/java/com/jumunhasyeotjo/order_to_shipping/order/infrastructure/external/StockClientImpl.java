package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.StockApiReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hub-product-stock-company", contextId = "OrderStockClient")
public interface StockClientImpl extends StockClient {

    @PostMapping("/decrement")
    boolean decreaseStock(StockApiReq orderProducts, @RequestHeader("Idempotency-Key") String idempotentKey);

    @PostMapping("/increment")
    void restoreStocks(StockApiReq orderProducts, @RequestHeader("Idempotency-Key") String idempotentKey);
}
