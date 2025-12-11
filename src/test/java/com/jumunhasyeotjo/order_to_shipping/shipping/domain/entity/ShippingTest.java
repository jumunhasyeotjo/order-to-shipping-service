package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingFixture;

class ShippingTest {

	@Test
	@DisplayName("배송 생성 시 배송 상태는 WAITING_AT_HUB이다")
	void createShipping_ShouldSetStatusToWaitingAtHub() {
		// given
		UUID orderId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		UUID receiverCompanyId = UUID.randomUUID();
		ShippingAddress address = ShippingAddress.of("서울시 강동구");
		UUID originHubId = UUID.randomUUID();
		UUID arrivalHubId = UUID.randomUUID();
		Integer totalRouteCount = 3;

		// when
		Shipping shipping = Shipping.create(id, orderId, receiverCompanyId, address, originHubId,
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
	@DisplayName("배송 취소는 WAITING_AT_HUB 상태 일때만 가능하다.")
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


}