package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.config;

import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository.JpaPaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.repository.PaymentRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRepositoryConfig {

    @Bean
    public PaymentRepository paymentRepository(JpaPaymentRepository JpaPaymentRepository) {
        return new PaymentRepositoryAdapter(JpaPaymentRepository);
    }
}
