package com.jumunhasyeotjo.order_to_shipping.payment.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.Money;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.PaymentStatus;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.TossPaymentMethod;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity{

	@GeneratedValue
	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID orderId;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "amount", column = @Column(name = "amount"))
	})
	private Money amount;

	@Enumerated(value = EnumType.STRING)
	private PaymentStatus status;

	@Enumerated(value = EnumType.STRING)
	private TossPaymentMethod method;

	@Column(nullable = false)
	private String tossPaymentKey;

	@Column(nullable = false)
	private String tossOrderId;

	private OffsetDateTime approvedAt;
	private OffsetDateTime cancelAt;

	@Builder
	public Payment(Money amount, UUID orderId, String tossPaymentKey, String tossOrderId) {
		this.amount = amount;
		this.orderId = orderId;
		this.tossPaymentKey = tossPaymentKey;
		this.tossOrderId = tossOrderId;
		this.status = PaymentStatus.PAYMENT_PENDING;
	}

	public void setPaymentResult(String status, String method, OffsetDateTime approvedAt){
		this.status = PaymentStatus.from(status);
		this.method = TossPaymentMethod.from(method);
		this.approvedAt = approvedAt;
	}

	public void failPayment(){
		this.status = PaymentStatus.ABORTED;
	}

}