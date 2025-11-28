package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", contextId = "OrderProductClient")
public interface ProductClientImpl extends ProductClient {

    @PostMapping("/order")
    List<ProductResult> findAllProducts(List<UUID> orderProducts);
}
