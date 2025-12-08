package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfoName;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingDelayedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.OrderClientImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.ShippingRouteGenerator;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingCreatedEvent;
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
	private final ShippingHistoryService shippingHistoryService;

	private final ShippingRepository shippingRepository;
	private final ShippingHistoryRepository shippingHistoryRepository;

	private final ShippingDelayAiMessageGenerator shippingDelayAiMessageGenerator;

	private final UserClient userClient;
	private final CompanyClient companyClient;

	private final ApplicationEventPublisher eventPublisher;
	private final OrderClientImpl orderClientImpl;

	@Transactional
	public UUID createShipping(CreateShippingCommand command) {
		log.info("배송 생성 시작: orderProductId={}", command.orderProductId());
		Company supplierCompany = companyClient.getCompany(command.supplierCompanyId());
		Company receiverCompany = companyClient.getCompany(command.receiverCompanyId());

		// 경로 생성
		List<Route> routes = shippingRouteGenerator.generateOrRebuildRoute(supplierCompany.hubId(), receiverCompany.hubId());

		// 배송 생성
		Shipping shipping = Shipping.create(command.orderProductId(), command.receiverCompanyId(),
			ShippingAddress.of(receiverCompany.address()),
			supplierCompany.hubId(), receiverCompany.hubId(), routes.size());

		// 배송 이력 생성
		List<ShippingHistory> shippingHistories = shippingHistoryService.createShippingHistoryList(shipping, routes, receiverCompany);
		shippingRepository.save(shipping);

		eventPublisher.publishEvent(new ShippingCreatedEvent(shipping.getId(), supplierCompany.hubId(),
			command.receiverCompanyId(), command.createdAt(), command.productInfo(), command.orderRequest(),
			shippingHistories.get(0).getDriverId(), shippingHistories));

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
		validateCancellableBy(command.role(), command.userBelong(), shipping.getOriginHubId());
		List<ShippingHistory> shippingHistories = shippingHistoryService.getShippingHistoryList(command.shippingId());

		shippingDomainService.cancelDelivery(shipping, shippingHistories);
		log.info("배송 취소 완료: shippingId={}", command.shippingId());
	}

	/**
	 * 배송 조회
	 */
	@Transactional(readOnly = true)
	public List<ShippingHistory> getShipping(GetShippingCommand command) {
		List<ShippingHistory> shippingHistories = shippingHistoryService.getShippingHistoryList(command.shippingId());

		return shippingHistories;
	}

	private void validateCancellableBy(UserRole userRole, String userBelong, UUID hubId) {
		if (userRole == UserRole.MASTER) {
			return;
		}

		if (userRole == UserRole.HUB_MANAGER && !Objects.equals(userBelong, hubId.toString())) {
			throw new BusinessException(FORBIDDEN);
		}
	}

	private Shipping getShippingById(UUID shippingId) {
		return shippingRepository.findById(shippingId).orElseThrow(
			() -> new BusinessException(NOT_FOUND_BY_ID)
		);
	}

	@Transactional(readOnly = true)
	public void delayShipping(DelayShippingCommand command) {
		ShippingHistory shippingHistory = shippingHistoryRepository.findByShippingId(command.shippingId())
				.orElseThrow(() -> new BusinessException(NOT_FOUND_BY_ID));

		validateDriver(command, shippingHistory);

		Shipping shipping = shippingRepository.findById(command.shippingId())
				.orElseThrow(() -> new BusinessException(NOT_FOUND_BY_ID));

		List<ProductInfoName> products = orderClientImpl.getProductsByCompanyOrderNameAndQuantity(shipping.getId());
		String message = shippingDelayAiMessageGenerator.generateMessage(products, command);

		eventPublisher.publishEvent(ShippingDelayedEvent.of(shipping.getId(), shipping.getReceiverCompanyId(), message));
	}

	private void validateDriver(DelayShippingCommand command, ShippingHistory shippingHistory) {
		if (!command.driverId().equals(shippingHistory.getDriverId())) {
			throw new BusinessException(FORBIDDEN);
		}
	}
}
