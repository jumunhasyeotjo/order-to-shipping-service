package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import java.util.UUID;

import org.springframework.data.geo.Distance;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_shipping_history")
public class ShippingHistory extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipping_id")
	private Shipping shipping;

	@Column(nullable = false)
	private UUID driverId;

	@Column(nullable = false)
	private Integer sequence;

	@Column(nullable = false)
	private String origin;

	@Column(nullable = false)
	private String destination;

	@Column(nullable = false)
	private ShippingHistoryStatus status;

	@Column(nullable = false)
	private Integer expectDistance;

	@Column(nullable = false)
	private Integer expectInterval;

	private Integer actualDistance;

	private Integer actualInterval;
}
