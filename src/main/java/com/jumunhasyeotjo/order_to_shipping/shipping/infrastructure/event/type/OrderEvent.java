package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type;

import java.util.Arrays;

import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderRolledBackEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum OrderEvent {
	CREATED("OrderCreatedEvent"),
	CANCELED("OrderCanceledEvent"),
	ROLLED_BACK("OrderRolledBackEvent");

	private final String eventName;

	public static OrderEvent ofString(String name) {
		return Arrays.stream(OrderEvent.values())
			.filter(e -> e.getEventName().equals(name))
			.findFirst()
			.orElseGet(() -> {
				log.warn("Unknown HubEvent name received: {}", name);
				return null;
			});
	}

}
