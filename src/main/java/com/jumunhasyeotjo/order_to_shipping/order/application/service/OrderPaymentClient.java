package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;

import java.util.UUID;

public interface OrderPaymentClient {
    @Metric(value = "PG 결제 승인 요청", level = Metric.Level.DEBUG)
    boolean confirmOrder(int amount, String tossPaymentKey, String tossOrderId, UUID orderId);
}
