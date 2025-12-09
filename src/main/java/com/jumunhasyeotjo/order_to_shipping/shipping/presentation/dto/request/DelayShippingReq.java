package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.DelayReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DelayShippingReq (
        @NotNull(message = "지연 사유는 필수 입니다.")
        DelayReason reason,

        @NotBlank(message = "현재 위치는 필수 입니다.")
        String location,

        @NotNull(message = "예정 지연 시간은 필수 입니다.")
        LocalDateTime delayTime,

        String etc
) {
}
