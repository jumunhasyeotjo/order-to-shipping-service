package com.jumunhasyeotjo.order_to_shipping.payment.application.command;

import java.util.UUID;

public record CancelPaymentCommand(
	UUID paymentId,
	String cancelReason
) {
}
