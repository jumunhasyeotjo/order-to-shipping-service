package com.jumunhasyeotjo.order_to_shipping.payment.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.PaymentPgRaw;

public interface PaymentPgRawRepository {
	void save(PaymentPgRaw paymentPgRaw);

	Optional<PaymentPgRaw> findByPaymentId(UUID paymentId);
}
