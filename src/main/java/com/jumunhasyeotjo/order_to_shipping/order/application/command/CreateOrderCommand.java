package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import java.util.List;

public record CreateOrderCommand (
        Long userId,
        String requestMessage,
        List<OrderProductReq> orderProducts
){
}
