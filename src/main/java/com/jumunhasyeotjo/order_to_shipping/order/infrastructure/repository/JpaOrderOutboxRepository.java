package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderOutboxRepository extends JpaRepository<OrderOutbox, UUID> {
    Optional<OrderOutbox> findByAggregateIdAndEventTypeAndIsPublishedFalse(UUID aggregateId, String eventType);
    List<OrderOutbox> findAllByIsPublishedFalse(Pageable pageable);
}
