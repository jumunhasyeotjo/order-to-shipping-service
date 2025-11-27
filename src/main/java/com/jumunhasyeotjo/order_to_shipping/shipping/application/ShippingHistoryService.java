package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentArrivedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentDepartedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShippingHistoryService {
	private final ShippingHistoryRepository shippingHistoryRepository;
	private final ShippingDomainService shippingDomainService;

	private final HubClient hubClient;
	private final ApplicationEventPublisher eventPublisher;
	private final DriverClient driverClient;

	/**
	 * 배송이력 생성
	 */
	public List<ShippingHistory> createShippingHistoryList(Shipping shipping, List<Route> routes,
		Company recevierCompany) {
		log.info("상세 배송 이력 생성: shippingId={}", shipping.getId());
		List<ShippingHistory> hubLegHistories = buildHubLegHistories(shipping, routes);

		ShippingHistory finalHistory = ShippingHistory.create(
			shipping,
			recevierCompany.driverId(),
			hubLegHistories.size() + 1,
			getHubNameFromId(recevierCompany.hubId()),
			recevierCompany.companyName(),
			recevierCompany.routeInfo()
		);

		List<ShippingHistory> shippingHistories = Stream.concat(hubLegHistories.stream(), Stream.of(finalHistory))
			.toList();
		shippingHistoryRepository.saveAll(shippingHistories);

		log.info("상세 배송이력 생성 완료: shippingHistory size={}", shippingHistories.size());
		return shippingHistories;
	}

	/**
	 * 배송 이력 출발
	 */
	public void departShippingHistory(DepartShippingHistoryCommand command) {
		log.info("배송 출발 처리 시작: shippingHistoryId={}", command.shippingHistoryId());

		ShippingHistory shippingHistory = getAuthorizedShippingHistory(
			command.shippingHistoryId(),
			command.userRole(),
			command.driverId()
		);

		shippingDomainService.departHistorySegment(shippingHistory);
		publishDepartedEvent(shippingHistory);

		log.info("배송 출발 처리 완료: shippingHistoryId={}", command.shippingHistoryId());
	}

	/**
	 * 배송 이력 도착
	 */
	public void arriveShippingHistory(ArriveShippingHistoryCommand command) {
		log.info("배송 도착 처리 시작: shippingHistoryId={}", command.shippingHistoryId());

		ShippingHistory shippingHistory = getAuthorizedShippingHistory(
			command.shippingHistoryId(),
			command.userRole(),
			command.driverId()
		);

		int totalRouteCount = shippingDomainService.arriveHistorySegment(shippingHistory, command.actualDistance());
		publishArrivedEvent(shippingHistory, totalRouteCount);

		log.info("배송 도착 처리 완료: shippingHistoryId={}", command.shippingHistoryId());
	}

	/**
	 * 배송자 수정
	 */
	public void changeDriver(ChangeDriverCommand command) {
		ShippingHistory shippingHistory = getShippingHistory(command.shippingHistoryId());
		checkMasterPermission(command.userRole());

		shippingHistory.changeDriver(command.newDriverId());
	}

	/**
	 * 배송자가 담당 배송내역 조회
	 */
	@Transactional(readOnly = true)
	public Page<ShippingHistory> getAssignedShippingHistories(GetAssignedShippingHistoriesCommand command,
		Pageable pageable) {
		return shippingHistoryRepository.findAllByDriverId(command.driverId(), pageable);
	}

	/**
	 * 배송에 대한 상세 배송이력 조회
	 */
	@Transactional(readOnly = true)
	public List<ShippingHistory> getShippingHistoryList(UUID shippingId) {
		List<ShippingHistory> shippingHistories = shippingHistoryRepository.findAllByShippingId(shippingId);
		if (shippingHistories.isEmpty())
			throw new BusinessException(NOT_FOUND_BY_ID);

		return shippingHistories;
	}

	private String getHubNameFromId(UUID hubId) {
		return hubClient.getHubName(hubId).orElseThrow(() -> new BusinessException(NOT_FOUND_BY_ID));
	}

	private void checkMasterPermission(UserRole userRole) {
		if (!userRole.equals(UserRole.MASTER)) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	private void checkPermission(ShippingHistory shippingHistory, UserRole userRole, Long driverId) {
		if (userRole.equals(UserRole.MASTER))
			return;

		boolean isFinalSegment = Objects.equals(shippingHistory.getShipping().getTotalRouteCount(),
			shippingHistory.getSequence());

		if ((!isFinalSegment && userRole.equals(UserRole.HUB_DRIVER))
			|| isFinalSegment && userRole.equals(UserRole.COMPANY_DRIVER)) {
			if (!shippingHistory.getDriverId().equals(driverId)) {
				throw new BusinessException(ErrorCode.FORBIDDEN);
			}
		} else
			throw new BusinessException(ErrorCode.FORBIDDEN);

	}

	private ShippingHistory getShippingHistory(UUID shippingHistoryId) {
		return shippingHistoryRepository.findById(shippingHistoryId).orElseThrow(
			() -> new BusinessException(ErrorCode.NOT_FOUND_BY_ID)
		);
	}

	private ShippingHistory getAuthorizedShippingHistory(UUID shippingHistoryId, UserRole userRole, Long driverId) {
		ShippingHistory shippingHistory = getShippingHistory(shippingHistoryId);
		checkPermission(shippingHistory, userRole, driverId);
		return shippingHistory;
	}

	private void publishDepartedEvent(ShippingHistory shippingHistory) {
		boolean isFirstSegment = shippingHistory.getSequence() == 1;

		eventPublisher.publishEvent(
			new ShippingSegmentDepartedEvent(
				shippingHistory.getOrigin(),
				shippingHistory.getShipping().getId(),
				isFirstSegment
			)
		);
	}

	private void publishArrivedEvent(ShippingHistory shippingHistory, int totalRouteCount) {
		boolean isFinalDestination = shippingHistory.getSequence() == (totalRouteCount - 1);

		eventPublisher.publishEvent(
			new ShippingSegmentArrivedEvent(
				shippingHistory.getDestination(),
				shippingHistory.getShipping().getId(),
				isFinalDestination
			)
		);
	}

	private List<ShippingHistory> buildHubLegHistories(Shipping shipping, List<Route> routes) {
		return IntStream.range(0, routes.size())
			.mapToObj(i -> {
				Route route = routes.get(i);
				return ShippingHistory.create(
					shipping,
					driverClient.assignDriver(route.departureHubId(), route.destinationHubId()),
					i + 1,
					getHubNameFromId(route.departureHubId()),
					getHubNameFromId(route.destinationHubId()),
					route.info()
				);
			})
			.toList();
	}

}