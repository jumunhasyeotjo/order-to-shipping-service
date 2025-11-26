package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CancelShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.ShippingRouteGenerator;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {
	private final ShippingRouteGenerator shippingRouteGenerator;
	private final ShippingDomainService shippingDomainService;
	private final ShippingRepository shippingRepository;
	private final ShippingHistoryService shippingHistoryService;

	private final UserClient userClient;

	@Transactional
	public UUID createShipping(CreateShippingCommand command) {
		log.info("배송 생성 시작: orderId={}", command.orderId());
		UUID originHubId = command.supplierCompany().hubId();
		UUID arrivalHubId = command.receiverCompany().hubId();

		// 경로 생성
		List<Route> routes = shippingRouteGenerator.generateOrRebuildRoute(originHubId, arrivalHubId);

		// 배송 생성
		Shipping shipping = Shipping.create(command.orderId(), command.receiverCompany().companyId(),
			ShippingAddress.of(command.receiverCompany().address()),
			command.receiverPhoneNumber(), command.receiverName(), command.supplierCompany().hubId(),
			command.receiverCompany().hubId(), routes.size());

		// 배송 이력 생성
		shippingHistoryService.createShippingHistoryList(shipping, routes, command.receiverCompany());
		shippingRepository.save(shipping);

		// todo 슬랙 메시지 보내기

		log.info("배송 생성 완료: shippingId={}", shipping.getId());
		return shipping.getId();
	}

	/**
	 * 배송 취소
	 */
	@Transactional
	public void cancelShipping(CancelShippingCommand command) {
		log.info("배송 취소 시작: shippingId={}", command.shippingId());
		Shipping shipping = getShippingById(command.shippingId());
		validateCancellableBy(command.role(), command.userId(), shipping.getOriginHubId());
		List<ShippingHistory> shippingHistories = shippingHistoryService.getShippingHistoryList(command.shippingId());

		shippingDomainService.cancelDelivery(shipping, shippingHistories);
		log.info("배송 취소 완료: shippingId={}", command.shippingId());
	}

	/**
	 * 배송 조회
	 */
	@Transactional(readOnly = true)
	public ShippingResult getShipping(GetShippingCommand command) {
		Shipping shipping = getShippingById(command.shippingId());
		List<ShippingHistory> shippingHistories = shippingHistoryService.getShippingHistoryList(command.shippingId());

		return ShippingResult.from(shipping, shippingHistories);
	}

	private void validateCancellableBy(UserRole userRole, Long userId, UUID hubId) {
		if (userRole == UserRole.MASTER) {
			return;
		}

		if (userRole == UserRole.HUB_MANAGER && !userClient.isManagingHub(userId, hubId)) {
			throw new BusinessException(FORBIDDEN);
		}
	}

	private Shipping getShippingById(UUID shippingId) {
		return shippingRepository.findById(shippingId).orElseThrow(
			() -> new BusinessException(NOT_FOUND_BY_ID)
		);
	}

}
