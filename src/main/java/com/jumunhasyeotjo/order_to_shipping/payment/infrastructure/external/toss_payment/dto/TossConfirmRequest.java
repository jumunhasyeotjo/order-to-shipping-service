package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossConfirmRequest {
    @NotBlank
    private String paymentKey;
    @NotBlank
    private String orderId;
    @Min(0)
    private Integer amount;
}