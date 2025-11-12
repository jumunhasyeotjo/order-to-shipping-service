package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "stock-service")
public interface StockClientImpl extends StockClient {
}
