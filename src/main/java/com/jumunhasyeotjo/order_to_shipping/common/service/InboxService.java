package com.jumunhasyeotjo.order_to_shipping.common.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxMessage;
import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxStatus;
import com.jumunhasyeotjo.order_to_shipping.common.entity.repository.InboxMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxMessageRepository inboxMessageRepository;

    @Transactional
    public void save(InboxMessage inboxMessage){
        inboxMessageRepository.save(inboxMessage);
    }

    public boolean existsByEventKey(String eventKey){
        return inboxMessageRepository.existsByEventKey(eventKey);
    }
}
