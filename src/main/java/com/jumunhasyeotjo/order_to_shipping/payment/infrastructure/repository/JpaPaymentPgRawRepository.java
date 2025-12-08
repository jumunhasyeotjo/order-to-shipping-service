package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.PaymentPgRaw;

@Repository
public interface JpaPaymentPgRawRepository extends JpaRepository<PaymentPgRaw, UUID> {
}
