package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(payment);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return jpaPaymentRepository.findByOrderId(orderId);
    }

    @Override
    public void deleteAll() {
        jpaPaymentRepository.deleteAll();
    }
}
