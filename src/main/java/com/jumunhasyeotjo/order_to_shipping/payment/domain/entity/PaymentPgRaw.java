package com.jumunhasyeotjo.order_to_shipping.payment.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "pg_response_json", columnDefinition = "jsonb")
	private String pgResponseJson;

	@Builder
	public PaymentPgRaw(UUID paymentId, String pgResponseJson) {
		this.paymentId = paymentId;
		this.pgResponseJson = pgResponseJson;
	}

	public void updatePgResponseJson(String pgResponseJson){
		this.pgResponseJson = pgResponseJson;
	}
}