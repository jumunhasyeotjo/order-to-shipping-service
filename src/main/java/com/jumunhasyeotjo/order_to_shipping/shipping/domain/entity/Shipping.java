package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_shipping")
public class Shipping extends BaseEntity {

	@Id
	private UUID id; // 주문 id

	@Column(nullable = false)
	private UUID receiverCompanyId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ShippingStatus shippingStatus;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "address", column = @Column(name = "shipping_address"))
	})
	private ShippingAddress shippingAddress;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "value", column = @Column(name = "receiver_phone_number"))
	})
	private PhoneNumber receiverPhoneNumber;

	@Column(nullable = false)
	private String receiverName;

	@Column(nullable = false)
	private UUID originHubId;

	@Column(nullable = false)
	private UUID arrivalHubId;

	private UUID currentHubId;

	/**
	 * 배송 생성 팩토리 메서드
	 */
	public static Shipping create(UUID orderId, UUID receiverCompanyId, ShippingAddress address,
		PhoneNumber receiverPhoneNumber, String receiverName, UUID originHubId, UUID arrivalHubId) {

		validateCreateArgs(orderId, receiverCompanyId, address, receiverPhoneNumber, receiverName, originHubId, arrivalHubId);

		Shipping shipping = new Shipping();
		shipping.id = orderId;
		shipping.receiverCompanyId = receiverCompanyId;
		shipping.shippingStatus = ShippingStatus.WAITING_AT_HUB;
		shipping.shippingAddress = address;
		shipping.receiverPhoneNumber = receiverPhoneNumber;
		shipping.receiverName = receiverName;
		shipping.originHubId = originHubId;
		shipping.arrivalHubId = arrivalHubId;
		shipping.currentHubId = originHubId;

		return shipping;
	}


	/**
	 * 출발허브에서 배송 출고
	 */
	public void dispatchFromOriginHub(){
		ShippingStatus newStatus = ShippingStatus.MOVING_TO_HUB;
		if (!canTransitionTo(newStatus)) {
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.currentHubId = null;
		this.shippingStatus = newStatus;
	}

	/**
	 * 도착허브에 도착
	 */
	public void arriveAtDestinationHub(){
		ShippingStatus newStatus = ShippingStatus.ARRIVED_AT_HUB;
		if (!canTransitionTo(newStatus)) {
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.currentHubId = arrivalHubId;
		this.shippingStatus = newStatus;
	}

	/**
	 * 도착허브에서 최종 목적지로 출발
	 */
	public void departFromDestinationHub(){
		ShippingStatus newStatus = ShippingStatus.MOVING_TO_COMPANY;
		if (!canTransitionTo(newStatus)) {
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.currentHubId = null;
		this.shippingStatus = newStatus;
	}

	/**
	 * 배송 도착
	 */
	public void completeDelivery(){
		ShippingStatus newStatus = ShippingStatus.DELIVERED;
		if (!canTransitionTo(newStatus)) {
			throw new BusinessException(INVALID_STATUS_TRANSITION);
		}

		this.currentHubId = null;
		this.shippingStatus = newStatus;
	}

	/**
	 * 현재 허브 위치 변경
	 */
	public void updateCurrentHubId(UUID currentHubId){
		if(!this.shippingStatus.equals(ShippingStatus.MOVING_TO_HUB)){
			throw new BusinessException(INVALID_STATE_FOR_MODIFICATION);
		}

		this.currentHubId = currentHubId;
	}

	/**
	 * 수령인 전화번호 변경
	 */
	public void changeReceiverPhoneNumber(PhoneNumber phoneNumber){
		if(this.shippingStatus.equals(ShippingStatus.DELIVERED)){
			throw new BusinessException(INVALID_STATE_FOR_MODIFICATION);
		}

		this.receiverPhoneNumber = phoneNumber;
	}

	/**
	 * 수령인 이름 변경
	 */
	public void changeReceiverName(String name){
		if(this.shippingStatus.equals(ShippingStatus.DELIVERED)){
			throw new BusinessException(INVALID_STATE_FOR_MODIFICATION);
		}

		this.receiverName = name;
	}


	private boolean canTransitionTo(ShippingStatus newStatus) {
		return this.shippingStatus.getSequence() + 1 == newStatus.getSequence();
	}

	private static void validateCreateArgs(UUID orderId,
		UUID receiverCompanyId,
		ShippingAddress address,
		PhoneNumber receiverPhoneNumber,
		String receiverName,
		UUID originHubId,
		UUID arrivalHubId) {
		if (orderId == null || receiverCompanyId == null || address == null ||
			receiverPhoneNumber == null || receiverName == null ||
			originHubId == null || arrivalHubId == null || receiverName.isBlank()) {
			throw new BusinessException(REQUIRED_VALUE_MISSING);
		}
		if (originHubId.equals(arrivalHubId)) {
			throw new BusinessException(INVALID_INPUT);
		}
	}


}
