package com.jumunhasyeotjo.order_to_shipping.payment.application.metric;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class PaymentMetrics {

    private final Counter paymentSuccessCount;
    private final Counter paymentFailCount;
    private final Counter paymentAmountKrwTotal;

    public PaymentMetrics(MeterRegistry registry) {
        this.paymentSuccessCount = Counter.builder("payment_success_total")
            .description("Total successful payments")
            .register(registry);

        this.paymentFailCount = Counter.builder("payment_fail_total")
            .description("Total failed payments")
            .register(registry);

        this.paymentAmountKrwTotal = Counter.builder("payment_amount_total")
            .description("Total payment amount (KRW)")
            .register(registry);
    }

    public void success(long amountKrw) {
        paymentSuccessCount.increment();
        paymentAmountKrwTotal.increment(amountKrw);
    }

    public void fail(String reason) {
        paymentFailCount.increment();
    }
}