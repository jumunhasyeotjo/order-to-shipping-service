package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CancelShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.ShippingRouteGenerator;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {
	@Mock
	private ShippingRouteGenerator shippingRouteGenerator;
	@Mock
	private ShippingDomainService shippingDomainService;

	@Mock
	private ShippingRepository shippingRepository;

	@Mock
	private ShippingHistoryRepository shippingHistoryRepository;

	@Mock
	private HubClient hubClient;

	@Mock
	private DriverClient driverClient;

	@InjectMocks
	private ShippingService shippingService;


	// 공용 더미 값
	UUID orderId = UUID.randomUUID();
	UUID originHubId = UUID.randomUUID();
	UUID midHubId = UUID.randomUUID();
	UUID arrivalHubId = UUID.randomUUID();
	UUID companyId = UUID.randomUUID();
	UUID shippingId = UUID.randomUUID();

	@Test
	@DisplayName("경로 기반으로 배송과 배송이력(허브 구간 + 최종 업체 구간)을 생성/저장한다")
	void createShipping_success() {
		// given
		Company supplier = new Company(
			UUID.randomUUID(),
			"공급업체",
			originHubId,
			"서울시 테스트구 테스트동 1",
			UUID.randomUUID(),
			new RouteInfo(30, 15)
		);
		Company receiver = new Company(
			companyId,
			"수령업체",
			arrivalHubId,
			"서울시 테스트구 테스트동 2",
			UUID.randomUUID(),
			new RouteInfo(40, 20)
		);
		PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");
		CreateShippingCommand command = new CreateShippingCommand(orderId, phoneNumber, "아무개", supplier, receiver);
		List<Route> routes = List.of(
			new Route(originHubId, midHubId, RouteInfo.of(10, 5)),
			new Route(midHubId, arrivalHubId, RouteInfo.of(20, 15))
		);

		when(shippingRouteGenerator.generateOrRebuildRoute(originHubId, arrivalHubId)).thenReturn(routes);
		when(hubClient.getHubName(any())).thenReturn(Optional.of("허브이름"));
		when(driverClient.assignDriver(any(UUID.class), any(UUID.class)))
			.thenReturn(UUID.randomUUID(), UUID.randomUUID());

		// when
		UUID resultId = shippingService.createShipping(command);

		// then
		verify(shippingRouteGenerator, times(1)).generateOrRebuildRoute(originHubId, arrivalHubId);

		// 배송 저장 호출 검증
		ArgumentCaptor<Shipping> shippingCaptor = ArgumentCaptor.forClass(Shipping.class);
		verify(shippingRepository, times(1)).save(shippingCaptor.capture());
		Shipping savedShipping = shippingCaptor.getValue();
		assertThat(savedShipping).isNotNull();

		// 배송이력 저장 호출 검증 (허브 2구간 + 최종 업체 1구간 = 3)
		ArgumentCaptor<List<ShippingHistory>> historiesCaptor = ArgumentCaptor.forClass(List.class);
		verify(shippingHistoryRepository, times(1)).saveAll(historiesCaptor.capture());
		List<ShippingHistory> savedHistories = historiesCaptor.getValue();
		assertThat(savedHistories).isNotNull();
		assertThat(savedHistories).hasSize(3);

		assertThat(resultId).isNotNull();
	}

	@Test
	@DisplayName("허브명을 못가져오면 배송 생성은 BusinessException을 던진다")
	void createShipping_whenHubNameMissing_shouldThrow() {
		// given
		Company supplier = new Company(
			UUID.randomUUID(),
			"공급업체",
			originHubId,
			"서울시 테스트구 테스트동 1",
			UUID.randomUUID(),
			new RouteInfo(30, 15)
		);
		Company receiver = new Company(
			companyId,
			"수령업체",
			arrivalHubId,
			"서울시 테스트구 테스트동 2",
			UUID.randomUUID(),
			new RouteInfo(40, 20)
		);
		PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");
		CreateShippingCommand command = new CreateShippingCommand(orderId, phoneNumber, "아무개", supplier, receiver);

		List<Route> routes = List.of(
			new Route(originHubId, arrivalHubId, RouteInfo.of(30, 20))
		);
		when(shippingRouteGenerator.generateOrRebuildRoute(originHubId, arrivalHubId)).thenReturn(routes);

		// 허브 이름 조회 실패
		when(hubClient.getHubName(any())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> shippingService.createShipping(command))
			.isInstanceOf(BusinessException.class);

		// 저장 동작이 일어나지 않았는지 확인
		verify(shippingRepository, never()).save(any());
		verify(shippingHistoryRepository, never()).saveAll(anyList());
	}

	@Test
	@DisplayName("배송id로 배송과 배송기록을 조회한다.")
	void getShipping_success() {
		// given
		GetShippingCommand command = mock(GetShippingCommand.class);
		when(command.shippingId()).thenReturn(shippingId);
		when(command.role()).thenReturn("ROLE_MASTER");
		when(command.userId()).thenReturn(1L);

		Shipping shipping = mock(Shipping.class);
		ShippingHistory h1 = mock(ShippingHistory.class);
		ShippingHistory h2 = mock(ShippingHistory.class);
		List<ShippingHistory> histories = List.of(h1, h2);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(shippingHistoryRepository.findAllByShippingId(shippingId)).thenReturn(histories);

		// when
		ShippingResult result = shippingService.getShipping(command);

		// then
		assertThat(result.shipping()).isSameAs(shipping);
		assertThat(result.shippingHistories()).isSameAs(histories);
		verify(shippingRepository, times(1)).findById(shippingId);
		verify(shippingHistoryRepository, times(1)).findAllByShippingId(shippingId);
		verifyNoMoreInteractions(shippingRepository);
		verifyNoMoreInteractions(shippingHistoryRepository);
	}


	@Test
	@DisplayName("배송 조회할때 존재하지 않으면 NOT_FOUND_BY_ID 예외가 터진다.")
	void getShipping_whenShippingIdNotExist_shouldThrowException() {
		// given
		GetShippingCommand command = mock(GetShippingCommand.class);
		when(command.shippingId()).thenReturn(shippingId);
		when(command.role()).thenReturn("ROLE_MASTER");
		when(command.userId()).thenReturn(1L);
		when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> shippingService.getShipping(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 ID로 데이터를 찾을 수 없습니다.");
		verify(shippingRepository, times(1)).findById(shippingId);
		verifyNoMoreInteractions(shippingRepository);
	}

	@Test
	@DisplayName("배송과 그 히스토리를 조회하여 도메인 서비스로 취소를 위임한다")
	void cancelShipping_success() {
		// given
		CancelShippingCommand command = mock(CancelShippingCommand.class);
		when(command.shippingId()).thenReturn(shippingId);
		when(command.role()).thenReturn("ROLE_MASTER");
		when(command.userId()).thenReturn(1L);

		Shipping shipping = mock(Shipping.class);
		ShippingHistory h1 = mock(ShippingHistory.class);
		ShippingHistory h2 = mock(ShippingHistory.class);
		List<ShippingHistory> histories = List.of(h1, h2);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(shippingHistoryRepository.findAllByShippingId(shippingId)).thenReturn(histories);

		// when
		shippingService.cancelShipping(command);

		// then
		verify(shippingDomainService, times(1)).cancelDelivery(shipping, histories);
	}


	@Test
	@DisplayName("배송 취소시 히스토리가 없으면 NOT_FOUND_BY_ID 예외가 발생한다.")
	void cancelShipping_whenShippingHistoryNotExist_shouldThrowException() {
		// given
		CancelShippingCommand command = mock(CancelShippingCommand.class);
		when(command.shippingId()).thenReturn(shippingId);
		when(command.role()).thenReturn("ROLE_MASTER");
		when(command.userId()).thenReturn(1L);

		Shipping shipping = mock(Shipping.class);
		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(shippingHistoryRepository.findAllByShippingId(shippingId)).thenReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> shippingService.cancelShipping(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 ID로 데이터를 찾을 수 없습니다.");
		verify(shippingDomainService, never()).cancelDelivery(any(), anyList());
	}

}