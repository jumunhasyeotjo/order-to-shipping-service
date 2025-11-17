package com.jumunhasyeotjo.order_to_shipping.payment.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private int amount;

    @Builder
    public Payment(UUID orderId, int amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment create(UUID orderId, int amount) {
        return Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .build();
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {
        this.status = PaymentStatus.FAIL;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
