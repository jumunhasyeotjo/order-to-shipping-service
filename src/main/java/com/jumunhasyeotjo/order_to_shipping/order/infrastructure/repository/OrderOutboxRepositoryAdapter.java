package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderOutbox;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderOutboxRepositoryAdapter implements OrderOutboxRepository {

    private final JpaOrderOutboxRepository jpaOrderOutboxRepository;

    @Override
    public OrderOutbox save(OrderOutbox orderOutbox) {
        return jpaOrderOutboxRepository.save(orderOutbox);
    }

    @Override
    public Optional<OrderOutbox> findByAggregateIdAndEventTypeAndIsPublishedFalse(UUID aggregateId, String eventType) {
        return jpaOrderOutboxRepository.findByAggregateIdAndEventTypeAndIsPublishedFalse(aggregateId, eventType);
    }

    @Override
    public List<OrderOutbox> findAllByIsPublishedFalse(Pageable pageable) {
        return jpaOrderOutboxRepository.findAllByIsPublishedFalse(pageable);
    }

}
