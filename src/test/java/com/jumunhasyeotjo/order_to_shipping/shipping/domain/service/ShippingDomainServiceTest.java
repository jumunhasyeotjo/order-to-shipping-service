package com.jumunhasyeotjo.order_to_shipping.shipping.domain.service;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingFixture;
import com.jumunhasyeotjo.order_to_shipping.shipping.fixtures.ShippingHistoryFixture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ShippingDomainServiceTest {

	private final ShippingDomainService service = new ShippingDomainService();

	@Test
	@DisplayName("첫 구간을 출발하면 배송상태가 MOVING_TO_HUB가 된다.")
	void depart_firstSegment_MovingToHub() {
		//given
		Shipping shipping = ShippingFixture.createDefault();
		ShippingHistory shippingHistory = ShippingHistoryFixture.createWithShippingAndSequence(shipping, 1);

		//when
		service.departHistorySegment(shippingHistory);

		//then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.MOVING_TO_HUB);
	}

	@Test
	@DisplayName("마지막 구간을 출발하면 배송상태가 MOVING_TO_COMPANY 된다.")
	void depart_lastSegment_MovingToCompany() {
		//given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();
		shipping.arriveAtDestinationHub();

		ShippingHistory shippingHistory = ShippingHistoryFixture.createWithShippingAndSequence(shipping,
			shipping.getTotalRouteCount());

		//when
		service.departHistorySegment(shippingHistory);

		//then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.MOVING_TO_COMPANY);
	}

	@Test
	@DisplayName("마지막 구간을 도착이면 배송상태가 DELIVERED 된다.")
	void arrive_lastSegment_Delivered() throws InterruptedException {
		//given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();
		shipping.arriveAtDestinationHub();
		shipping.departFromDestinationHub();

		ShippingHistory shippingHistory = ShippingHistoryFixture.createWithShippingAndSequence(shipping,
			shipping.getTotalRouteCount());
		shippingHistory.markAsDeparted();
		Integer actualDistance = 110;

		Thread.sleep(60000); // 배송시간 지연

		//when
		service.arriveHistorySegment(shippingHistory, actualDistance);

		//then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
	}

	@Test
	@DisplayName("마지막 전 구간을 도착하면 배송상태가 ARRIVED_AT_HUB 된다.")
	void arrive_beforeLast_ArrivedAtHub() throws InterruptedException {
		//given
		Shipping shipping = ShippingFixture.createDefault();
		shipping.dispatchFromOriginHub();

		Integer sequence = shipping.getTotalRouteCount() - 1;
		ShippingHistory shippingHistory = ShippingHistoryFixture.createWithShippingAndSequence(shipping, sequence);
		shippingHistory.markAsDeparted();
		Integer actualDistance = 110;

		Thread.sleep(60000); // 배송시간 지연

		//when
		service.arriveHistorySegment(shippingHistory, actualDistance);

		//then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.ARRIVED_AT_HUB);
	}

	@Test
	@DisplayName("모든 히스토리가 동일한 Shipping에 속하면 배송과 히스토리가 모두 취소된다")
	void cancel_allBelonging_cancelAll() {
		//given
		Shipping shipping = ShippingFixture.createDefault();
		ShippingHistory shippingHistory1 = ShippingHistoryFixture.createWithShippingAndSequence(shipping, 1);
		ShippingHistory shippingHistory2 = ShippingHistoryFixture.createWithShippingAndSequence(shipping, 2);

		//when
		service.cancelDelivery(shipping, List.of(shippingHistory1, shippingHistory2));

		//then
		assertThat(shipping.getShippingStatus()).isEqualTo(ShippingStatus.CANCELED);
		assertThat(shippingHistory1.getStatus()).isEqualTo(ShippingHistoryStatus.CANCELED);
		assertThat(shippingHistory2.getStatus()).isEqualTo(ShippingHistoryStatus.CANCELED);
	}

	@Test
	@DisplayName("히스토리 중 다른 Shipping을 참조하면 BusinessException이 발생한다")
	void cancel_mismatch_throwsBusinessException() {
		//given
		Shipping shipping1 = ShippingFixture.createDefault();
		Shipping shipping2 = ShippingFixture.createDefault();
		ShippingHistory shippingHistory1 = ShippingHistoryFixture.createWithShippingAndSequence(shipping1, 1);
		ShippingHistory shippingHistory2 = ShippingHistoryFixture.createWithShippingAndSequence(shipping2, 2);

		// when & then
		assertThatThrownBy(() -> service.cancelDelivery(shipping1, List.of(shippingHistory1, shippingHistory2)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("배송이력과 배송이 일치하지 않습니다.");
	}
}