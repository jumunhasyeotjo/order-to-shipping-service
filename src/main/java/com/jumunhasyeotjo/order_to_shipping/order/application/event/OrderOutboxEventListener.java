package com.jumunhasyeotjo.order_to_shipping.order.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderOutbox;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCancelledEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderRolledBackEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxEventListener {

    private final OrderOutboxRepository orderOutboxRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OrderCreatedEvent event) {
        saveOutbox("ORDER_CREATED", event.getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OrderRolledBackEvent event) {
        saveOutbox("ORDER_ROLLEDBACK", event.getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OrderCancelledEvent event) {
        saveOutbox("ORDER_CANCELLED", event.getOrderId(), event);
    }


    private void saveOutbox(String eventType, UUID aggregateId, Object payload) {
        try {
            OrderOutbox outbox = OrderOutbox.builder()
                    .aggregateType("ORDER")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            orderOutboxRepository.save(outbox);
            
        } catch (JsonProcessingException e) {
            log.error("Outbox payload conversion failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}