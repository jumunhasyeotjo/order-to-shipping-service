package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.event.CouponIssueEvent;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.CouponEventProducerService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponEventProducer implements CouponEventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendIssueEvent(CouponIssueEvent event) {

        ProducerRecord<String, Object> record =
            new ProducerRecord<>("order", event.couponId().toString(), event);

        record.headers().add(new RecordHeader("type", "issueCoupon".getBytes()));

        kafkaTemplate.send(record);
    }
}
