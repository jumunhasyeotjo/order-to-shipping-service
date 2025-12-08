package com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProcessPaymentReq(
	@Schema(description = "주문 금액", example = "50000")
	Integer amount,
	@Schema(description = "토스 결제 키", example = "tgen_202512081825032SKb9")
	String tossPaymentKey,
	@Schema(description = "토스 주문 아이디", example = "MC4xODA4NTc2MzI4NTIy")
	String tossOrderId,
	@Schema(description = "주문 아이디")
	UUID orderId

) {
}
