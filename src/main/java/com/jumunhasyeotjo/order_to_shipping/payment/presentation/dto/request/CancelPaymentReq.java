package com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CancelPaymentReq(
	@Schema(description = "결제 취소 사유")
	String cancelReason
) {
}
