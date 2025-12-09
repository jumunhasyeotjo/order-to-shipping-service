package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import java.util.UUID;

import lombok.Getter;
@Getter
public class ShippingMsgReqEvent extends ShippingDomainEvent {
	private final UUID originHubId;
	private final UUID receiverCompanyId;
	private final String orderIdMessage;
	private final String infoMessage;
	private final String etaMessage;
	private final Long driverId;

	public ShippingMsgReqEvent(UUID originHubId, UUID receiverCompanyId, String orderIdMessage, String infoMessage,
		String etaMessage, Long driverId) {
		this.originHubId = originHubId;
		this.receiverCompanyId = receiverCompanyId;
		this.orderIdMessage = orderIdMessage;
		this.infoMessage = infoMessage;
		this.etaMessage = etaMessage;
		this.driverId = driverId;
	}
}