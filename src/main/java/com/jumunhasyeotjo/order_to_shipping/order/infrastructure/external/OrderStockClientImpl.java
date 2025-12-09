package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderStockClient;
import com.library.passport.entity.ApiRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", path = "/api/v1/stocks/internal")
public interface OrderStockClientImpl extends OrderStockClient {

    @PostMapping("/decrement")
    ApiRes<Boolean> decreaseStock(List<OrderProductReq> productList, @RequestHeader("Idempotency-Key") UUID orderId);
}
