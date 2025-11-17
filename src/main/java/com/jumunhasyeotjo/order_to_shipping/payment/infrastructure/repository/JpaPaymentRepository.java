package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
}
