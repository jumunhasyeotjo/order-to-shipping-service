package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {
	boolean existsByTossPaymentKey(String paymentKey);

	Optional<Payment> findByOrderId(UUID orderId);
}
