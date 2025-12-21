package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateOrderReq(
        String requestMessage,

        @NotEmpty(message = "주문 상품은 필수 입력값 입니다.")
        List<OrderProductReq> orderProducts,

        UUID couponId

//        @NotEmpty(message = "tossPaymentKey 는 필수 입력값 입니다.")
//        String tossPaymentKey,
//
//        @NotEmpty(message = "tossOrderId 는 필수 입력값 입니다.")
//        String tossOrderId
) {
}