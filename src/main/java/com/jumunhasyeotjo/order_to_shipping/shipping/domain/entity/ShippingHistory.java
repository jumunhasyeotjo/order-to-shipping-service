package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
	private Long driverId;

	@Column(nullable = false)
	private Integer sequence;

	@Column(nullable = false)
	private String origin;

	@Column(nullable = false)
	private String destination;

	@Column(nullable = false)
	private ShippingHistoryStatus status;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "distance", column = @Column(name = "expect_distance")),
		@AttributeOverride(name = "interval", column = @Column(name = "expect_interval"))
	})
	private RouteInfo expectRouteInfo;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "distance", column = @Column(name = "actual_distance")),
		@AttributeOverride(name = "interval", column = @Column(name = "actual_interval"))
	})
	private RouteInfo actualRouteInfo;

	private LocalDateTime departedAt;

	private LocalDateTime arrivedAt;


	/**
	 * 배송 생성 팩토리 메서드
	 */
	public static ShippingHistory create(Shipping shipping, Long driverId, Integer sequence, String origin, String destination,
		RouteInfo expectRouteInfo) {

		validateCreateArgs(shipping, driverId, sequence, origin, destination, expectRouteInfo);

		ShippingHistory shippingHistory = new ShippingHistory();
		shippingHistory.shipping = shipping;
		shippingHistory.status = ShippingHistoryStatus.WAITING;
		shippingHistory.driverId = driverId;
		shippingHistory.sequence = sequence;
		shippingHistory.origin = origin;
		shippingHistory.destination = destination;
		shippingHistory.expectRouteInfo = expectRouteInfo;

		return shippingHistory;
	}

	/**
	 * 배송자 수정
	 */
	public void changeDriver(Long driverId){
		if(!this.status.equals(ShippingHistoryStatus.WAITING)){
			throw new BusinessException(INVALID_STATE_FOR_MODIFICATION);
		}

		this.driverId = driverId;
	}

	/**
	 * 배송 출발
	 */
	public void markAsDeparted(){
		ShippingHistoryStatus newStatus = ShippingHistoryStatus.MOVING_TO_HUB;
		if(!canTransitionTo(newStatus)){
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.status = newStatus;
		this.departedAt = LocalDateTime.now();
	}

	/**
	 * 배송 도착
	 */
	public void markAsArrived(Integer actualDistance){
		ShippingHistoryStatus newStatus = ShippingHistoryStatus.ARRIVED;
		if(!canTransitionTo(newStatus)){
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.status = newStatus;
		this.arrivedAt = LocalDateTime.now();
		this.actualRouteInfo = RouteInfo.ofActualRoute(actualDistance, this.departedAt, arrivedAt);
	}

	/**
	 * 배송취소
	 */
	public void cancel(){
		if(!this.status.equals(ShippingHistoryStatus.WAITING)){
			throw new BusinessException(INVALID_STATE_FOR_MODIFICATION);
		}

		this.status = ShippingHistoryStatus.CANCELED;
	}

	private boolean canTransitionTo(ShippingHistoryStatus newStatus) {
		return this.status.getSequence() + 1 == newStatus.getSequence();
	}

	private static void validateCreateArgs(Shipping shipping, Long driverId, Integer sequence, String origin, String destination,
		RouteInfo expectRouteInfo) {
		if (shipping == null || driverId == null || sequence == null ||
			origin == null || destination == null ||
			expectRouteInfo == null || origin.isBlank() || destination.isBlank()) {
			throw new BusinessException(REQUIRED_VALUE_MISSING);
		}

		if (sequence <= 0) {
			throw new BusinessException(INVALID_INPUT);
		}
	}

}
