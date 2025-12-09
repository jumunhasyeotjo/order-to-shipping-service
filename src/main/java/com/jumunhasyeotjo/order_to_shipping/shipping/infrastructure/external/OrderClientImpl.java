package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfoName;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OrderClientImpl implements OrderClient {
    private final OrderService orderService;

    public OrderClientImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public List<ProductInfo> getProductsByCompanyOrder(UUID companyOrderId) {
        return List.of(new ProductInfo(UUID.randomUUID(), 2));
    }

    @Override
    public List<ProductInfoName> getProductsByVendorOrderNameAndQuantity(UUID id) {
        return orderService.getCompanyOrderItemsName(id).stream()
				.map(p -> new ProductInfoName(p.name(), p.quantity())).toList();
    }
}
