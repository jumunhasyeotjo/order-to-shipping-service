package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

public enum ShippingHistoryStatus {
	WAITING(1),
	MOVING_TO_HUB(2),
	ARRIVED(3),
	CANCELED(-1);
	private final int sequence;

	ShippingHistoryStatus(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence(){
		return this.sequence;
	}
}
