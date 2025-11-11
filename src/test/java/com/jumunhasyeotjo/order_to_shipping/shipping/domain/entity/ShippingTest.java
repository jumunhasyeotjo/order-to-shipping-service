package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingFixture;

class ShippingTest {

	@Test
	@DisplayName("배송 생성 시 배송 상태는 WAITING_AT_HUB이다")
	void createShipping_ShouldSetStatusToWaitingAtHub() {
		// given
		UUID orderId = UUID.randomUUID();
		UUID receiverCompanyId = UUID.randomUUID();
		ShippingAddress address = ShippingAddress.of("서울시 강동구");
		PhoneNumber receiverPhoneNumber = PhoneNumber.of("010-1234-5678");
		String receiverName = "수령인";
		UUID originHubId = UUID.randomUUID();
		UUID arrivalHubId = UUID.randomUUID();
		Integer totalRouteCount = 3;

		// when
		Shipping shipping = Shipping.create(orderId, receiverCompanyId, address, receiverPhoneNumber, receiverName, originHubId,
			arrivalHubId, totalRouteCount);

		// then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.WAITING_AT_HUB);
	}

	@Test
	@DisplayName("배송 취소 시 배송 상태는 CANCELED이다.")
	void cancelShipping_ShouldSetStatusToCanceled() {
		// given
		Shipping shipping = ShippingFixture.createDefault();

		// when
		shipping.cancel();

		// then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.CANCELED);
	}

	@Test
	@DisplayName("배송 취소는 WAITING_AT_HUB 상태 일떄만 가능하다.")
	void cancelShipping_OnlyWhenWaitingAtHub() {
		// given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();

		// when & then
		assertThatThrownBy(shipping::cancel)
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("배송이 이미 시작되어 취소할 수 없습니다.");
	}

	@Test
	@DisplayName("배송 상태 변경은 순차적으로만 가능하다")
	void changeShippingStatus_ShouldAllowOnlySequentialTransition(){
		// given
		Shipping shipping = ShippingFixture.createDefault();

		// when & then
		assertThatThrownBy(shipping::arriveAtDestinationHub)
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 상태로 전환할 수 없습니다.");
	}

	@Test
	@DisplayName("수령인 전화번호는 배달이 완료된 이후에 변경할 수 없다.")
	void changeReceiverPhoneNumber_ShouldNotAllowAfterDelivered(){
		// given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();
		shipping.arriveAtDestinationHub();
		shipping.departFromDestinationHub();
		shipping.completeDelivery();

		PhoneNumber phoneNumber = PhoneNumber.of("010-1111-2222");

		// when & then
		assertThatThrownBy(() -> shipping.changeReceiverPhoneNumber(phoneNumber))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("현재 상태에서는 정보를 수정할 수 없습니다.");
	}

	@Test
	@DisplayName("수령인 이름은 배달이 완료된 이후에 변경할 수 없다.")
	void changeReceiverName_ShouldNotAllowAfterDelivered(){
		// given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();
		shipping.arriveAtDestinationHub();
		shipping.departFromDestinationHub();
		shipping.completeDelivery();

		String newName = "아무개";

		// when & then
		assertThatThrownBy(() -> shipping.changeReceiverName(newName))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("현재 상태에서는 정보를 수정할 수 없습니다.");
	}


}