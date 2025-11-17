package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
	private final DriverClient driverClient;

	/**
	 * 배송생성
	 */
	public void createShippingHistoryList(Shipping shipping, List<Route> routes, Company recevierCompany) {
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
	}

	/**
	 * 배송 이력 출발
	 */
	public void departShippingHistory(DepartShippingHistoryCommand command) {
		log.info("배송 출발 처리 시작: shippingHistoryId={}", command.shippingHistoryId());

		ShippingHistory shippingHistory = getShippingHistory(command.shippingHistoryId());
		checkPermission(shippingHistory, command.userRole(), command.driverId());

		shippingDomainService.departHistorySegment(shippingHistory);

		log.info("배송 출발 처리 완료: shippingHistoryId={}", command.shippingHistoryId());
	}

	/**
	 * 배송 이력 도착
	 */
	public void arriveShippingHistory(ArriveShippingHistoryCommand command) {
		log.info("배송 도착 처리 시작: shippingHistoryId={}", command.shippingHistoryId());

		ShippingHistory shippingHistory = getShippingHistory(command.shippingHistoryId());
		checkPermission(shippingHistory, command.userRole(), command.driverId());

		shippingDomainService.arriveHistorySegment(shippingHistory, command.actualDistance());

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