package com.jumunhasyeotjo.order_to_shipping.payment.application;

import com.jumunhasyeotjo.order_to_shipping.common.event.EventPublisher;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.event.PaymentCompletedEvent;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final EventPublisher eventPublisher;

    private final PaymentRepository paymentRepository;

    // 결제 처리
    @Transactional
    public void processPayment(UUID orderId, int amount, List<OrderProduct> orderProducts) {
        Payment payment = Payment.create(orderId, amount);
        payment.complete();

        paymentRepository.save(payment);

        eventPublisher.publish(PaymentCompletedEvent.of(payment, orderProducts));
    }
}
