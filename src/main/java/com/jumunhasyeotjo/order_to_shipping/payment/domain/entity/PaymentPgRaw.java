package com.jumunhasyeotjo.order_to_shipping.payment.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_payment_pg_raw")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentPgRaw extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(nullable = false)
	private UUID paymentId;

	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String pgResponseJson;

	@Builder
	public PaymentPgRaw(UUID paymentId, String pgResponseJson) {
		this.paymentId = paymentId;
		this.pgResponseJson = pgResponseJson;
	}
}