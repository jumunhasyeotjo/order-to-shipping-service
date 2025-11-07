package com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingFixture;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingHistoryFixture;

class ShippingHistoryTest {

	@Test
	@DisplayName("배송 기록 생성 시 상태는 WAITING이다")
	void createShippingHistory_ShouldSetStatusToWaiting() {
		// given
		Shipping shipping = ShippingFixture.createDefault();
		UUID driverId = UUID.randomUUID();
		Integer sequence = 1;
		String origin = "출발지";
		String destination = "도착지";
		RouteInfo expectRouteInfo = RouteInfo.of(100, 100);

		// when
		ShippingHistory shippingHistory = ShippingHistory.create(shipping, driverId, sequence, origin, destination,
			expectRouteInfo);

		// then
		assertThat(shippingHistory.getStatus()).isEqualTo(ShippingHistoryStatus.WAITING);
	}

	@Test
	@DisplayName("배송 기록 생성 시 sequene는 0이하 일 수 없다.")
	void createShippingHistory_WhenSequenceIsZeroOrNegative_ShouldThrowException() {
		// given
		Shipping shipping = ShippingFixture.createDefault();
		UUID driverId = UUID.randomUUID();
		Integer sequence = -1;
		String origin = "출발지";
		String destination = "도착지";
		RouteInfo expectRouteInfo = RouteInfo.of(100, 100);

		// when & then
		assertThatThrownBy(
			() -> ShippingHistory.create(shipping, driverId, sequence, origin, destination, expectRouteInfo))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("잘못된 요청입니다.");
	}

	@Test
	@DisplayName("배송자 수정은 WAITING 상태 일때만 가능하다.")
	void changeDriver_WhenStatusIsNotWaiting_ShouldThrowException() {
		//given
		ShippingHistory shippingHistory = ShippingHistoryFixture.createDefault();
		shippingHistory.departDelivery();
		UUID newDriverId = UUID.randomUUID();

		// when & then
		assertThatThrownBy(() -> shippingHistory.changeDriver(newDriverId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("현재 상태에서는 정보를 수정할 수 없습니다.");
	}

	@Test
	@DisplayName("배송 상태 변경은 순차적으로만 가능하다.")
	void changeStatus_WhenTransitionIsNotSequential_ShouldThrowException() {
		//given
		ShippingHistory shippingHistory = ShippingHistoryFixture.createDefault();
		Integer actualDistance = 120;

		// when & then
		assertThatThrownBy(() -> shippingHistory.arriveDelivery(actualDistance))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 상태로 전환할 수 없습니다.");
	}

	@Test
	@DisplayName("배송출발하면 상태가 MOVING_TO_HUB로 바뀌고 출발시간이 입력된다.")
	void departDelivery_ShouldChangeStatusToMovingAndSetDepartedAt() {
		//given
		ShippingHistory shippingHistory = ShippingHistoryFixture.createDefault();

		//when
		shippingHistory.departDelivery();

		//then
		assertThat(shippingHistory.getStatus()).isEqualTo(ShippingHistoryStatus.MOVING_TO_HUB);
		assertThat(shippingHistory.getDepartedAt()).isNotNull();
	}

	@Test
	@DisplayName("배송이 도착하면 상태가 ARRIVED로 바뀌고 실제 경로 정보가 저장된다.")
	void arriveDelivery_ShouldChangeStatusToArrivedAndSaveActualRouteInfo() throws InterruptedException {
		//given
		ShippingHistory shippingHistory = ShippingHistoryFixture.createDefault();
		shippingHistory.departDelivery();
		Integer actualDistance = 120;

		Thread.sleep(60000); // 배송시간 지연

		//when
		shippingHistory.arriveDelivery(120);

		//then
		assertThat(shippingHistory.getStatus()).isEqualTo(ShippingHistoryStatus.ARRIVED);
		assertThat(shippingHistory.getArrivedAt()).isNotNull();
		assertThat(shippingHistory.getActualRouteInfo().getDistance()).isEqualTo(actualDistance);
		assertThat(shippingHistory.getActualRouteInfo().getInterval()).isNotNull();
	}

}