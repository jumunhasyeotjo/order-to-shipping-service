package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossCancelRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossConfirmRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossPaymentResponse;

@Profile("perf")
@Component
public class MockTossPaymentsClient implements TossPaymentsClient {

    @Override
    public TossPaymentResponse confirm(TossConfirmRequest request) {
        return mockConfirmResponse(request);
    }

    @Override
    public TossPaymentResponse cancel(String paymentKey, TossCancelRequest request) {
        return mockCancelResponse(paymentKey);
    }

    @Override
    public TossPaymentResponse getPaymentInfo(String paymentKey) {
        return mockGetPaymentInfo(paymentKey);
    }

    private TossPaymentResponse mockConfirmResponse(TossConfirmRequest request) {
        return TossPaymentResponse.builder()
            .paymentKey(request.getPaymentKey())
            .orderId(request.getOrderId())
            .status("DONE")
            .method("CARD")
            .approvedAt(OffsetDateTime.from(LocalDateTime.now()))
            .totalAmount(request.getAmount())
            .build();
    }

    private TossPaymentResponse mockCancelResponse(String paymentKey) {
        return TossPaymentResponse.builder()
            .paymentKey(paymentKey)
            .status("CANCELED")
            .approvedAt(OffsetDateTime.from(LocalDateTime.now()))
            .build();
    }

    private TossPaymentResponse mockGetPaymentInfo(String paymentKey) {
        return TossPaymentResponse.builder()
            .paymentKey(paymentKey)
            .status("DONE")
            .build();
    }
}