package com.jumunhasyeotjo.order_to_shipping.payment.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findByOrderId(UUID id);

    void deleteAll();
}
