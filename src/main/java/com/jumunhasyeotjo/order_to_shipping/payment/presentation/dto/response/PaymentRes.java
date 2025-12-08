package com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossPaymentResponse;

public record PaymentRes(
	Integer amount,
	String paymentMethod,
	String status,
	UUID paymentId,
	String paymentKey,
	String orderName,
	OffsetDateTime approvedAt
) {
	public static PaymentRes of(TossPaymentResponse response, UUID paymentId){
		return new PaymentRes(response.getTotalAmount(), response.getMethod(), response.getStatus(), paymentId, response.getPaymentKey(),
			response.getOrderName(), response.getApprovedAt());
	}
}
