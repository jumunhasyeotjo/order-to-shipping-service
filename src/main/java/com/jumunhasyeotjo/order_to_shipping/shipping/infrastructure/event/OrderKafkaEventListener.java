package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.util.KafkaUtil;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderCanceledEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderEventHandler;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderRolledBackEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaEventListener {

	private final OrderEventHandler orderEventHandler;
	private final ObjectMapper objectMapper;

	@KafkaListener(
		topics = "${spring.kafka.topics.order}",
		groupId = "${spring.kafka.consumer.group-id}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void listen(
		@Payload String event,
		@Header(name = "__TypeId__", required = false) String fullTypeName
	) {
		try {
			String simpleClassName = KafkaUtil.getClassName(fullTypeName);
			dispatch(event, simpleClassName);
		}catch (Exception e){
			log.error("Error processing event: {}", e.getMessage(), e);
		}
	}

	public void dispatch(String payload, String simpleClassName) throws JsonProcessingException {
		switch (OrderEvent.ofString(simpleClassName)){
			case CREATED ->{
				OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);
				orderEventHandler.orderCreated(orderCreatedEvent);
			}
			case CANCELED -> {
				OrderCanceledEvent orderCanceledEvent = objectMapper.readValue(payload, OrderCanceledEvent.class);
				orderEventHandler.orderCanceled(orderCanceledEvent);
			}
			case ROLLED_BACK -> {
				OrderRolledBackEvent orderRolledBackEvent = objectMapper.readValue(payload, OrderRolledBackEvent.class);
				orderEventHandler.orderRolledBack(orderRolledBackEvent);
			}
		}
	}
}
