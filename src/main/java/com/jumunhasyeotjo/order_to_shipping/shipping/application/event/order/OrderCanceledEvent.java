package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCanceledEvent(
	UUID orderId,
	String cancelReason,
	LocalDateTime occurredAt
) {
}
