package com.jumunhasyeotjo.order_to_shipping.payment.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;

public interface PaymentRepository{
	void save(Payment payment);
	Optional<Payment> findByTossPaymentKey(String paymentKey);
	Optional<Payment> findByOrderId(UUID orderId);
	Optional<Payment> findById(UUID paymentId);
}
