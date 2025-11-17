package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CancelShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

	@Mock
	private ShippingRouteGenerator shippingRouteGenerator;

	@Mock
	private ShippingDomainService shippingDomainService;

	@Mock
	private ShippingRepository shippingRepository;

	@Mock
	private ShippingHistoryService shippingHistoryService;

	@Mock
	private UserClient userClient;

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
			1L,
			new RouteInfo(30, 15)
		);
		Company receiver = new Company(
			companyId,
			"수령업체",
			arrivalHubId,
			"서울시 테스트구 테스트동 2",
			2L,
			new RouteInfo(40, 20)
		);
		PhoneNumber phoneNumber = PhoneNumber.of("010-1234-5678");
		CreateShippingCommand command = new CreateShippingCommand(orderId, phoneNumber, "아무개", supplier, receiver);

		List<Route> routes = List.of(
			new Route(originHubId, midHubId, RouteInfo.of(10, 5)),
			new Route(midHubId, arrivalHubId, RouteInfo.of(20, 15))
		);

		when(shippingRouteGenerator.generatorRoute(originHubId, arrivalHubId)).thenReturn(routes);

		// when
		UUID resultId = shippingService.createShipping(command);

		// then=
		verify(shippingRouteGenerator, times(1)).generatorRoute(originHubId, arrivalHubId);

		ArgumentCaptor<Shipping> shippingCaptor = ArgumentCaptor.forClass(Shipping.class);
		verify(shippingRepository, times(1)).save(shippingCaptor.capture());
		Shipping savedShipping = shippingCaptor.getValue();
		assertThat(savedShipping).isNotNull();

		verify(shippingHistoryService, times(1))
			.createShippingHistoryList(savedShipping, routes, receiver);

		assertThat(resultId).isNotNull();
	}

	@Test
	@DisplayName("배송id로 배송과 배송기록을 조회한다.")
	void getShipping_success() {
		// given
		GetShippingCommand command =
			new GetShippingCommand(shippingId, UserRole.MASTER, 1L);

		Shipping shipping = mock(Shipping.class);
		ShippingHistory h1 = mock(ShippingHistory.class);
		ShippingHistory h2 = mock(ShippingHistory.class);
		List<ShippingHistory> histories = List.of(h1, h2);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(shippingHistoryService.getShippingHistoryList(shippingId)).thenReturn(histories);

		// when
		ShippingResult result = shippingService.getShipping(command);

		// then
		assertThat(result.shipping()).isSameAs(shipping);
		assertThat(result.shippingHistories()).isSameAs(histories);
		verify(shippingRepository, times(1)).findById(shippingId);
		verify(shippingHistoryService, times(1)).getShippingHistoryList(shippingId);
		verifyNoMoreInteractions(shippingRepository, shippingHistoryService);
	}

	@Test
	@DisplayName("배송 조회할때 존재하지 않으면 NOT_FOUND_BY_ID 예외가 터진다.")
	void getShipping_whenShippingIdNotExist_shouldThrowException() {
		// given
		GetShippingCommand command =
			new GetShippingCommand(shippingId, UserRole.MASTER, 1L);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> shippingService.getShipping(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 ID로 데이터를 찾을 수 없습니다.");
		verify(shippingRepository, times(1)).findById(shippingId);
		verifyNoMoreInteractions(shippingRepository);
	}

	@Test
	@DisplayName("MASTER 권한 사용자는 허브 상관 없이 배송을 취소할 수 있다.")
	void cancelShipping_success_master() {
		// given
		CancelShippingCommand command =
			new CancelShippingCommand(shippingId, UserRole.MASTER, 1L);

		Shipping shipping = mock(Shipping.class);
		List<ShippingHistory> histories = List.of(
			mock(ShippingHistory.class),
			mock(ShippingHistory.class)
		);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(shippingHistoryService.getShippingHistoryList(shippingId)).thenReturn(histories);

		// when
		shippingService.cancelShipping(command);

		// then
		verify(userClient, never()).isManagingHub(anyLong(), any());
		verify(shippingDomainService, times(1)).cancelDelivery(shipping, histories);
	}

	@Test
	@DisplayName("허브담당자는 자신이 담당하는 허브의 배송만 취소할 수 있다.")
	void cancelShipping_success_hubManagerWithManagingHub() {
		// given
		Long userId = 10L;
		CancelShippingCommand command =
			new CancelShippingCommand(shippingId, UserRole.HUB_MANAGER, userId);

		Shipping shipping = mock(Shipping.class);
		when(shipping.getOriginHubId()).thenReturn(originHubId);

		List<ShippingHistory> histories = List.of(
			mock(ShippingHistory.class),
			mock(ShippingHistory.class)
		);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(userClient.isManagingHub(userId, originHubId)).thenReturn(true);
		when(shippingHistoryService.getShippingHistoryList(shippingId)).thenReturn(histories);

		// when
		shippingService.cancelShipping(command);

		// then
		verify(userClient, times(1)).isManagingHub(userId, originHubId);
		verify(shippingDomainService, times(1)).cancelDelivery(shipping, histories);
	}

	@Test
	@DisplayName("허브담당자가 담당하지 않는 허브 배송을 취소하려 하면 FORBIDDEN 예외가 발생한다.")
	void cancelShipping_whenHubManagerNotManagingHub_shouldThrowForbidden() {
		// given
		Long userId = 10L;
		CancelShippingCommand command = new CancelShippingCommand(shippingId, UserRole.HUB_MANAGER, userId);

		Shipping shipping = mock(Shipping.class);
		when(shipping.getOriginHubId()).thenReturn(originHubId);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
		when(userClient.isManagingHub(userId, originHubId)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> shippingService.cancelShipping(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("접근이 거부되었습니다.");

		verify(userClient, times(1)).isManagingHub(userId, originHubId);
		verify(shippingHistoryService, never()).getShippingHistoryList(any());
		verify(shippingDomainService, never()).cancelDelivery(any(), anyList());
	}

	@Test
	@DisplayName("배송 취소 시 배송이 존재하지 않으면 NOT_FOUND_BY_ID 예외가 발생한다.")
	void cancelShipping_whenShippingNotFound_shouldThrowException() {
		// given
		CancelShippingCommand command =
			new CancelShippingCommand(shippingId, UserRole.MASTER, 1L);

		when(shippingRepository.findById(shippingId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> shippingService.cancelShipping(command))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("해당 ID로 데이터를 찾을 수 없습니다.");

		verify(shippingRepository, times(1)).findById(shippingId);
		verifyNoMoreInteractions(shippingRepository);
		verifyNoInteractions(shippingHistoryService, userClient, shippingDomainService);
	}

	@Test
	@DisplayName("배송 취소 시 히스토리 조회 단계에서 예외가 발생하면 그대로 전파한다.")
	void cancelShipping_whenHistoryServiceThrows_shouldPropagate() {
		// given
		CancelShippingCommand command =
			new CancelShippingCommand(shippingId, UserRole.MASTER, 1L);

		Shipping shipping = mock(Shipping.class);
		when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));

		BusinessException historyException = new BusinessException(ErrorCode.NOT_FOUND_BY_ID);
		when(shippingHistoryService.getShippingHistoryList(shippingId))
			.thenThrow(historyException);

		// when & then
		assertThatThrownBy(() -> shippingService.cancelShipping(command))
			.isSameAs(historyException);

		verify(shippingHistoryService, times(1)).getShippingHistoryList(shippingId);
		verify(shippingDomainService, never()).cancelDelivery(any(), anyList());
	}
}