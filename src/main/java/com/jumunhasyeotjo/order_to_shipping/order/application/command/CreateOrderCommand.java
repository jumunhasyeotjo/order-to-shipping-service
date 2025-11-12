package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.List;

public record CreateOrderCommand (
        Long userId,
        Integer totalPrice,
        String requestMessage,
        List<OrderProductReq> orderProducts
){
}
