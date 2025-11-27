package com.jumunhasyeotjo.order_to_shipping.order.fixtures;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderFixtures {

    public static Order getOrder() {
        List<OrderProduct> products = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        products.add(OrderProduct.create(productId, 1000, 1, "상품1"));

        List<OrderCompany> orderCompanies = new ArrayList<>();
        orderCompanies.add(OrderCompany.create(UUID.randomUUID(), products));

        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;
        return Order.create(orderCompanies, 1L, companyId, requestMessage, totalPrice);
    }
}
