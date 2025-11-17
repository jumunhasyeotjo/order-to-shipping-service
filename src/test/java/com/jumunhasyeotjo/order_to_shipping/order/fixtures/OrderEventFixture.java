package com.jumunhasyeotjo.order_to_shipping.order.fixtures;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderEventFixture {

    public static CreateOrderCommand getCreateOrderCommand() {
        return new CreateOrderCommand(1L, 10000, "주문",
                new ArrayList<>(List.of(new OrderProductReq(UUID.randomUUID(), 10))));
    }

    public static List<ProductResult> getProductResults(CreateOrderCommand request) {
        return new ArrayList<>(List.of(new ProductResult(request.orderProducts().get(0).productId(),
                "상품1", 1000, request.orderProducts().get(0).quantity())));
    }
}
