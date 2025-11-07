package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;

public enum ShippingStatus {
	WAITING_AT_HUB(1),        // 허브 대기중
	MOVING_TO_HUB(2),      // 허브 이동중
	ARRIVED_AT_HUB(3),        // 목적지 허브 도착
	MOVING_TO_COMPANY(4),  // 업체 이동 중
	DELIVERED(5),           // 배송 완료
	CANCELED(-1);          // 배송 취소

	private final int sequence;

	ShippingStatus(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence(){
		return this.sequence;
	}
}
