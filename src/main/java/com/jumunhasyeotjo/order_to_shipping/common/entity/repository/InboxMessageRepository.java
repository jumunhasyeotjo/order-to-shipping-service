package com.jumunhasyeotjo.order_to_shipping.common.entity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxMessage;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, Long> {

    Optional<InboxMessage> findByEventKey(String eventKey);

    boolean existsByEventKey(String eventKey);
}