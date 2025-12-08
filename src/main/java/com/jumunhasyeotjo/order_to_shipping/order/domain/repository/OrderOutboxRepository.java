package com.jumunhasyeotjo.order_to_shipping.order.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderOutbox;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderOutboxRepository {
    OrderOutbox save(OrderOutbox orderOutbox);
    Optional<OrderOutbox> findByAggregateIdAndEventTypeAndIsPublishedFalse(UUID aggregateId, String eventType);
    List<OrderOutbox> findAllByIsPublishedFalse(Pageable pageable);
}
