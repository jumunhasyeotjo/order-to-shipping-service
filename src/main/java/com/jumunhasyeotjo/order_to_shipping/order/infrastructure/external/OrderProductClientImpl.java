package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductListRes;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config.OrderFeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", path = "/internal/api/v1/products", configuration = OrderFeignConfig.class)
public interface OrderProductClientImpl extends OrderProductClient {

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackFindAllProducts")
    @CircuitBreaker(name = "default")
    @PostMapping("/order")
    ProductListRes findAllProducts(List<UUID> orderProducts);


    default ProductListRes fallbackFindAllProducts(List<UUID> orderProducts, Throwable t) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
