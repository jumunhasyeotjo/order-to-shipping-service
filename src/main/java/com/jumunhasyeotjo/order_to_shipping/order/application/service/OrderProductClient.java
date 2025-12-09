package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.library.passport.entity.ApiRes;

import java.util.List;
import java.util.UUID;

public interface OrderProductClient {
    ApiRes<List<ProductResult>> findAllProducts(List<UUID> orderProducts);
}
