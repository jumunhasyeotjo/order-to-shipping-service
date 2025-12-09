package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
	UUID orderId,
	LocalDateTime orderCreatedTime,
	String productInfo,
	String requestMessage,
	List<UUID> supplierCompanyId,
	UUID receiverCompanyId,
	LocalDateTime occurredAt
) {
}
