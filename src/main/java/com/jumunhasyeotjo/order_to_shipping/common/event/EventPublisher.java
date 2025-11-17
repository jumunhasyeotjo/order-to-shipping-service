package com.jumunhasyeotjo.order_to_shipping.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        log.debug("이벤트 발행 - Type: {}, AggregateId: {}",
                event.getClass().getSimpleName(),
                event.getAggregateId()
        );

        applicationEventPublisher.publishEvent(event);
    }
}
