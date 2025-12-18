package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record OrderCreatedEvent(
	UUID orderId,
	LocalDateTime orderCreatedTime,
	String requestMessage,
	UUID receiverCompanyId,
	LocalDateTime occurredAt,
	List<VendingOrder> vendingOrders
) {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class VendingOrder {
		private UUID vendingOrderId;
		private UUID supplierCompanyId;
		private String productInfo;
	}
}
