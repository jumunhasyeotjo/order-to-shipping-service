package com.jumunhasyeotjo.order_to_shipping.payment.application.command;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.Money;

public record ProcessPaymentCommand(
	UUID orderId,
	Money amount,
	String tossPaymentKey,
	String tossOrderId
) {
}
