package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.event.CouponIssueEvent;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.CouponEventProducerService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponEventProducer implements CouponEventProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.order}")
    private String topic;

    public void sendIssueEvent(CouponIssueEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, event.couponId().toString(), payload);

            record.headers().add(new RecordHeader("eventType", "ISSUE_COUPON".getBytes()));

            kafkaTemplate.send(record);

        } catch (Exception e) {
            throw new RuntimeException("Kafka 직렬화 실패", e);
        }
    }
}
