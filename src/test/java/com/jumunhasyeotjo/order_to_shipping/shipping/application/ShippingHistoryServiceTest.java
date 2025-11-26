package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.ArriveShippingHistoryCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.ChangeDriverCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.DepartShippingHistoryCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetAssignedShippingHistoriesCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ShippingHistoryServiceTest {

	@Mock
	private ShippingHistoryRepository shippingHistoryRepository;

	@Mock
	private ShippingDomainService shippingDomainService;

	@Mock
	private HubClient hubClient;

	@Mock
	private DriverClient driverClient;

	@InjectMocks
	private ShippingHistoryService service;

	@Test
	@DisplayName("배송 생성 시 허브 구간 + 최종 구간까지 배송 이력이 생성되고 저장된다.")
	void createShippingHistoryList_shouldCreateAllSegmentsAndSave() {
		// given
		Shipping shipping = mock(Shipping.class);
		UUID shippingId = UUID.randomUUID();
		when(shipping.getId()).thenReturn(shippingId);

		UUID hubA = UUID.randomUUID();
		UUID hubB = UUID.randomUUID();
		UUID hubC = UUID.randomUUID();

		RouteInfo routeInfo1 = RouteInfo.of(100, 60);
		RouteInfo routeInfo2 = RouteInfo.of(200, 120);

		Route route1 = new Route(hubA, hubB, routeInfo1);
		Route route2 = new Route(hubB, hubC, routeInfo2);
		List<Route> routes = List.of(route1, route2);

		UUID companyId = UUID.randomUUID();
		UUID receiverHubId = UUID.randomUUID();
		Long receiverDriverId = 1L;
		RouteInfo finalRouteInfo = RouteInfo.of(10, 10);

		Company receiverCompany = new Company(
			companyId,
			"수령회사",
			receiverHubId,
			"서울시 어딘가",
			receiverDriverId,
			finalRouteInfo
		);

		given(hubClient.getHubName(hubA)).willReturn(Optional.of("허브A"));
		given(hubClient.getHubName(hubB)).willReturn(Optional.of("허브B"));
		given(hubClient.getHubName(hubC)).willReturn(Optional.of("허브C"));
		given(hubClient.getHubName(receiverHubId)).willReturn(Optional.of("수령허브"));

		given(driverClient.assignDriver(hubA, hubB)).willReturn(1L);
		given(driverClient.assignDriver(hubB, hubC)).willReturn(2L);

		ArgumentCaptor<List<ShippingHistory>> captor = ArgumentCaptor.forClass(List.class);

		// when
		service.createShippingHistoryList(shipping, routes, receiverCompany);

		// then
		verify(shippingHistoryRepository).saveAll(captor.capture());
		List<ShippingHistory> saved = captor.getValue();

		// 허브 구간 2개 + 최종 구간 1개 = 3개
		assertThat(saved).hasSize(3);

		assertThat(saved.get(0).getSequence()).isEqualTo(1);
		assertThat(saved.get(1).getSequence()).isEqualTo(2);
		assertThat(saved.get(2).getSequence()).isEqualTo(3);
	}

	@Test
	@DisplayName("배송 이력 출발 처리 시 권한 검증 후 도메인 서비스에 위임한다.")
	void departShippingHistory_shouldValidateAndDelegateToDomainService() {
		// given
		UUID historyId = UUID.randomUUID();
		Long driverId = 1L;

		DepartShippingHistoryCommand command =
			new DepartShippingHistoryCommand(historyId, UserRole.HUB_DRIVER, driverId);

		Shipping shipping = mock(Shipping.class);
		when(shipping.getTotalRouteCount()).thenReturn(3);

		ShippingHistory history = mock(ShippingHistory.class);
		when(history.getShipping()).thenReturn(shipping);
		when(history.getSequence()).thenReturn(1);
		when(history.getDriverId()).thenReturn(driverId);

		given(shippingHistoryRepository.findById(historyId))
			.willReturn(Optional.of(history));

		// when
		service.departShippingHistory(command);

		// then
		verify(shippingDomainService).departHistorySegment(history);
	}

	@Test
	@DisplayName("배송 이력 도착 처리 시 권한 검증 후 도메인 서비스에 위임한다.")
	void arriveShippingHistory_shouldValidateAndDelegateToDomainService() {
		// given
		UUID historyId = UUID.randomUUID();
		Long driverId = 1L;
		int actualDistance = 150;

		ArriveShippingHistoryCommand command =
			new ArriveShippingHistoryCommand(historyId, UserRole.HUB_DRIVER, driverId, actualDistance);

		Shipping shipping = mock(Shipping.class);
		when(shipping.getTotalRouteCount()).thenReturn(3);

		ShippingHistory history = mock(ShippingHistory.class);
		when(history.getShipping()).thenReturn(shipping);
		when(history.getSequence()).thenReturn(1);
		when(history.getDriverId()).thenReturn(driverId);

		given(shippingHistoryRepository.findById(historyId))
			.willReturn(Optional.of(history));

		// when
		service.arriveShippingHistory(command);

		// then
		verify(shippingDomainService).arriveHistorySegment(history, actualDistance);
	}

	@Test
	@DisplayName("배송자 수정은 MASTER 권한에서만 가능하다.")
	void changeDriver_masterOnly() {
		// given
		UUID historyId = UUID.randomUUID();
		Long newDriverId = 2L;

		ChangeDriverCommand command =
			new ChangeDriverCommand(historyId, UserRole.MASTER, newDriverId);

		ShippingHistory history = mock(ShippingHistory.class);
		given(shippingHistoryRepository.findById(historyId))
			.willReturn(Optional.of(history));

		// when
		service.changeDriver(command);

		// then
		verify(history).changeDriver(newDriverId);
	}

	@Test
	@DisplayName("MASTER가 아닌 사용자가 배송자를 수정하려 하면 FORBIDDEN 예외가 발생한다.")
	void changeDriver_nonMaster_shouldThrowForbidden() {
		// given
		UUID historyId = UUID.randomUUID();
		Long newDriverId = 2L;

		ChangeDriverCommand command =
			new ChangeDriverCommand(historyId, UserRole.HUB_DRIVER, newDriverId);

		ShippingHistory history = mock(ShippingHistory.class);
		given(shippingHistoryRepository.findById(historyId))
			.willReturn(Optional.of(history));

		// when & then
		assertThatThrownBy(() -> service.changeDriver(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("접근이 거부되었습니다.");
	}

	@Test
	@DisplayName("배송자 배정 받은 배송 이력 페이지를 조회한다.")
	void getAssignedShippingHistories_shouldDelegateToRepository() {
		// given
		Long driverId = 1L;
		GetAssignedShippingHistoriesCommand command =
			new GetAssignedShippingHistoriesCommand(driverId);

		Pageable pageable = mock(Pageable.class);
		Page<ShippingHistory> page = new PageImpl<>(List.of(mock(ShippingHistory.class)));

		given(shippingHistoryRepository.findAllByDriverId(driverId, pageable))
			.willReturn(page);

		// when
		Page<ShippingHistory> result = service.getAssignedShippingHistories(command, pageable);

		// then
		assertThat(result).isEqualTo(page);
		verify(shippingHistoryRepository).findAllByDriverId(driverId, pageable);
	}

	@Test
	@DisplayName("배송 아이디로 상세 배송 이력을 조회한다.")
	void getShippingHistoryList_success() {
		// given
		UUID shippingId = UUID.randomUUID();
		List<ShippingHistory> histories = List.of(
			mock(ShippingHistory.class),
			mock(ShippingHistory.class)
		);

		given(shippingHistoryRepository.findAllByShippingId(shippingId))
			.willReturn(histories);

		// when
		List<ShippingHistory> result = service.getShippingHistoryList(shippingId);

		// then
		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("배송 아이디로 조회 시 이력이 없으면 NOT_FOUND_BY_ID 예외를 던진다.")
	void getShippingHistoryList_notFound_shouldThrow() {
		// given
		UUID shippingId = UUID.randomUUID();
		given(shippingHistoryRepository.findAllByShippingId(shippingId))
			.willReturn(List.of());

		// when & then
		assertThatThrownBy(() -> service.getShippingHistoryList(shippingId))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 ID로 데이터를 찾을 수 없습니다.");
	}
}