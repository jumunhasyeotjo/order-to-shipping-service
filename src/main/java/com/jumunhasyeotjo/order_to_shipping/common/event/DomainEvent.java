package com.jumunhasyeotjo.order_to_shipping.common.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime getOccurredAt();
}
