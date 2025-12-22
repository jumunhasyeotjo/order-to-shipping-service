package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.config.TossFeignConfig;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossCancelRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossConfirmRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossPaymentResponse;

@Profile("!perf")
@FeignClient(
        name = "tossPaymentsClient",
        url = "${toss.base-url}",
        configuration = TossFeignConfig.class
)
public interface TossPaymentsClient {
    @PostMapping("/v1/payments/confirm")
    TossPaymentResponse confirm(@RequestBody TossConfirmRequest request);

    @GetMapping("/v1/payments/{paymentKey}")
    TossPaymentResponse getPaymentInfo(@PathVariable String paymentKey);

    @PostMapping("/v1/payments/{paymentKey}/cancel")
    TossPaymentResponse cancel(@PathVariable String paymentKey, @RequestBody TossCancelRequest request);

}