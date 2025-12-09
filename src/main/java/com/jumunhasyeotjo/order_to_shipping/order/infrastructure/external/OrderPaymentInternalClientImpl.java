package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderPaymentClient;
import com.jumunhasyeotjo.order_to_shipping.payment.application.PaymentService;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.ProcessPaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPaymentInternalClientImpl implements OrderPaymentClient {

    private final PaymentService paymentService;

    @Override
    public boolean confirmOrder(int amount, String tossPaymentKey, String tossOrderId, UUID orderId) {
        UUID result = paymentService.processPayment(new ProcessPaymentCommand(orderId, amount, tossPaymentKey, tossOrderId));
        return result != null;
    }
}
