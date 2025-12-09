package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request;

import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.CancelReason;
import jakarta.validation.constraints.NotNull;

public record CancelOrderReq(
        @NotNull(message = "주문 취소 사유는 필수 입니다.")
        CancelReason reason
) {
}
