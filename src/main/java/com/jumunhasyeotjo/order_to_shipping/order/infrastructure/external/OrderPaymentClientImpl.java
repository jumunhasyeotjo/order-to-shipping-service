package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderPaymentClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderPaymentClientImpl implements OrderPaymentClient {

    @Override
    public boolean confirmOrder(int amount, UUID orderId) {
        return true;
    }
}
