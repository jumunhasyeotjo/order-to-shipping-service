package com.jumunhasyeotjo.order_to_shipping.payment.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentCompletedEvent implements DomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final int amount;
    private final List<OrderProduct> orderProducts;
    private final LocalDateTime occurredAt;

    public static PaymentCompletedEvent of(Payment payment, List<OrderProduct> orderProducts) {
        return new PaymentCompletedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                orderProducts,
                LocalDateTime.now()
        );
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }
}

