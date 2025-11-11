package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderReq(
        @NotNull(message = "총 가격은 필수 입력값 입니다.")
        Integer totalPrice,

        String requestMessage,

        @NotEmpty(message = "주문 상품은 필수 입력값 입니다.")
        List<OrderProductReq> orderProducts
) {
}