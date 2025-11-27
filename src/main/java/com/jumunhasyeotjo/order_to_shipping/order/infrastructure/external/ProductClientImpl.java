package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductClientImpl implements ProductClient {

    @Override
    public List<ProductResult> findAllProducts(List<UUID> orderProducts) {
        return List.of(new ProductResult(orderProducts.get(0), UUID.randomUUID(), "상품", 1000));
    }
}
