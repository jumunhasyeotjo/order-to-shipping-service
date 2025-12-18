package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderStockClient;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config.OrderFeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hub-product-stock-company", path = "/internal/api/v1/stocks", configuration = OrderFeignConfig.class)
public interface OrderStockClientImpl extends OrderStockClient {

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackDecreaseStock")
    @CircuitBreaker(name = "default")
    @PostMapping("/decrement")
    ExternalExists decreaseStock(List<OrderProductReq> productList, @RequestHeader("Idempotency-Key") String orderId);


    default ExternalExists fallbackDecreaseStock(List<OrderProductReq> productList, String orderId, Throwable t) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
