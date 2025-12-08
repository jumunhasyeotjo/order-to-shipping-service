package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.PaymentPgRaw;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentPgRawRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentPgRawRepositoryAdapter implements PaymentPgRawRepository {
	private final JpaPaymentPgRawRepository jpaPaymentPgRawRepository;
	@Override
	public void save(PaymentPgRaw paymentPgRaw) {
		jpaPaymentPgRawRepository.save(paymentPgRaw);
	}
}
