package com.jumunhasyeotjo.order_to_shipping.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID getAggregateId();
    LocalDateTime getOccurredAt();
}
