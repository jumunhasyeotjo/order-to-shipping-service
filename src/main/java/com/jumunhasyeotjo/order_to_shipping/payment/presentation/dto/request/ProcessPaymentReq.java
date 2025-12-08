package com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request;

import java.util.UUID;

public record ProcessPaymentReq(
	Integer amount,
	String tossPaymentKey,
	String tossOrderId,
	UUID orderId

) {
}
