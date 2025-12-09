package com.jumunhasyeotjo.order_to_shipping.common.inbox;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxMessage;
import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxStatus;
import com.jumunhasyeotjo.order_to_shipping.common.entity.repository.InboxMessageRepository;
import com.jumunhasyeotjo.order_to_shipping.common.inbox.InboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxRepository inboxRepository;

    @Transactional
    public void process(String eventKey, String eventType, String payload, Runnable handler) {
        if (inboxRepository.existsByEventKeyAndStatus(eventKey, InboxStatus.PROCESSING)) {
            return;
        }

        InboxMessage inboxMessage = InboxMessage.builder()
            .eventKey(eventKey)
            .eventType(eventType)
            .payload(payload).build();

        if(!inboxRepository.existsByEventKey(eventKey)){
            inboxRepository.save(inboxMessage);
        }

        try {
            handler.run();

            inboxMessage.markProcessed();
        } catch (RuntimeException e) {
            inboxMessage.markFailed(e.getMessage());
            throw e;
        }
    }
}