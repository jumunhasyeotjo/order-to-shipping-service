package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingDomainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.common.util.JsonUtil;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingMsgReqEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaShippingMessageEventPublisher {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final JsonUtil jsonUtil;

	@Value("${spring.kafka.topics.shipping-message}")
	private String shippingMessageTopic;

	public void publishEvent(ShippingDomainEvent event) {
		kafkaTemplate.send(shippingMessageTopic, jsonUtil.toJson(event));
	}
}