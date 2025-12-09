package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type;

import java.util.Arrays;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum HubEvent{
	CREATED("HubCreatedEvent"),
	DELETED("HubDeletedEvent"),
	NAME_UPDATE("HubNameUpdatedEvent"),
	ROUTE_CREATED("HubRouteCreatedEvent"),
	ROUTE_DELETED( "HubRouteDeletedEvent");

	private final String eventName;

	public static HubEvent ofString(String name) {
		return Arrays.stream(HubEvent.values())
			.filter(e -> e.getEventName().equals(name))
			.findFirst()
			.orElseGet(() -> {
				log.warn("Unknown HubEvent name received: {}", name);
				return null;
			});
	}

}
