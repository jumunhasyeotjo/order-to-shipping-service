package com.jumunhasyeotjo.order_to_shipping.common.inbox;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxMessage;
import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxStatus;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InboxRepository extends JpaRepository<InboxMessage, Long> {

	Optional<InboxMessage> findByEventKey(String eventKey);

	boolean existsByEventKeyAndStatus(String eventKey, InboxStatus status);

	boolean existsByEventKey(String eventKey);

}