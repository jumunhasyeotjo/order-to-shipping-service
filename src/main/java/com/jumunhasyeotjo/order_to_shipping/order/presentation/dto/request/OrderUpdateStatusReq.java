package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateStatusReq(
        @NotNull(message = "상태 정보는 필수 입력값 입니다.")
        OrderStatus status
) {
}
