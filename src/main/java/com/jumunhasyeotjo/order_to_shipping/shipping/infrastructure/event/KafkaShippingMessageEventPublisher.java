package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event;

import com.jumunhasyeotjo.order_to_shipping.common.util.JsonUtil;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaShippingMessageEventPublisher {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final JsonUtil jsonUtil;

	@Value("${spring.kafka.topics.shipping-message}")
	private String shippingMessageTopic;

	public void publishEvent(ShippingDomainEvent event, String eventType) {
		String payload = jsonUtil.toJson(event);

		Message<String> message = MessageBuilder
			.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, shippingMessageTopic)
			.setHeader("eventType", eventType)
			.build();

		kafkaTemplate.send(message);
	}
}