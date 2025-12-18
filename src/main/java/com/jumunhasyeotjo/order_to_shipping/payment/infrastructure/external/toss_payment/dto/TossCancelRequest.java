package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto;

public record TossCancelRequest(
	String cancelReason
) {
	public static TossCancelRequest of(String cancelReason){
		return new TossCancelRequest(cancelReason);
	}
}
