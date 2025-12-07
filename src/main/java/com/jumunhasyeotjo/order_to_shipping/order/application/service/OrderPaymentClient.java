package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.UUID;

public interface OrderPaymentClient {
    boolean confirmOrder(int amount, UUID orderId);
}
