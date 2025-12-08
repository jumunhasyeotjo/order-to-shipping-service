package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository.JpaShippingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {
	private final JpaPaymentRepository jpaPaymentRepository;

	@Override
	public void save(Payment payment) {
		jpaPaymentRepository.save(payment);
	}

	@Override
	public Optional<Payment> findByTossPaymentKey(String paymentKey) {
		return jpaPaymentRepository.findByTossPaymentKey(paymentKey);
	}

	@Override
	public Optional<Payment> findByOrderId(UUID orderId) {
		return jpaPaymentRepository.findByOrderId(orderId);
	}

	@Override
	public Optional<Payment> findById(UUID paymentId) {
		return jpaPaymentRepository.findById(paymentId);
	}
}
