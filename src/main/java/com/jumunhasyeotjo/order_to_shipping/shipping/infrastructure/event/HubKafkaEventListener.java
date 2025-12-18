package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.util.KafkaUtil;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubDeletedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubEventHandler;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubNameUpdatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type.HubEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubKafkaEventListener {

	private final HubEventHandler hubEventHandler;
	private final ObjectMapper objectMapper;

	@KafkaListener(
		topics = "${spring.kafka.topics.hub}",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void listen(
		@Payload String event,
		@Header(name = "eventType", required = false) String eventType
	) {
		try {
			dispatch(event, eventType);
		}catch (Exception e){
			log.error("Error processing event: {}", e.getMessage(), e);
		}
	}

	public void dispatch(String payload, String eventType) throws JsonProcessingException {
		switch (HubEvent.ofString(eventType)){
			case CREATED ->{
				HubCreatedEvent hubCreatedEvent = objectMapper.readValue(payload, HubCreatedEvent.class);
				hubEventHandler.hubCreated(hubCreatedEvent);
			}
			case DELETED ->{
				HubDeletedEvent hubDeletedEvent = objectMapper.readValue(payload, HubDeletedEvent.class);
				hubEventHandler.hubDeleted(hubDeletedEvent);
			}
			case NAME_UPDATE -> {
				HubNameUpdatedEvent hubNameUpdatedEvent = objectMapper.readValue(payload, HubNameUpdatedEvent.class);
				hubEventHandler.hubNameUpdated(hubNameUpdatedEvent);
			}
			case ROUTE_CREATED -> {
				hubEventHandler.hubRouteCreated();
			}
			case ROUTE_DELETED -> {
				hubEventHandler.hubRouteDeleted();
			}
		}
	}
}
