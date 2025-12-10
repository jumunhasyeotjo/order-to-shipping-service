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
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.OrderClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentArrivedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.event.ShippingSegmentDepartedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.HubIdCache;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.HubNameCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShippingHistoryService {
	private final ShippingHistoryRepository shippingHistoryRepository;
	private final ShippingDomainService shippingDomainService;

	private final ApplicationEventPublisher eventPublisher;

	private final DriverClient driverClient;
	private final OrderClient orderClient;
	private final StockClient stockClient;

	private final HubIdCache hubIdCache;
	private final HubNameCache hubNameCache;

	/**
	 * 배송이력 생성
	 */
	public List<ShippingHistory> createShippingHistoryList(Shipping shipping, List<Route> routes,
		Company recevierCompany) {
		log.info("상세 배송 이력 생성: shippingId={}", shipping.getId());
		List<ShippingHistory> hubLegHistories = buildHubLegHistories(shipping, routes);

		ShippingHistory finalHistory = ShippingHistory.create(
			shipping,
			driverClient.assignCompanyDriver(recevierCompany.hubId()),
			hubLegHistories.size() + 1,
			getHubNameFromId(recevierCompany.hubId()),
			recevierCompany.name(),
			//todo 예상 경로 받기
			RouteInfo.of(500, 120)
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
		if (shippingHistory.getSequence() != 1) { // 첫 허브 재고는 차감하지않음(주문할때 이미 차감)
			decreaseOriginHubStock(shippingHistory.getShipping().getId(), shippingHistory.getOrigin());
		}

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
		Shipping shipping = shippingHistory.getShipping();
		if (!Objects.equals(shippingHistory.getSequence(), shipping.getTotalRouteCount())) { // 최종경로는 목적지이므로 허브 재고차감 하지않음
			increaseDestinationHubStock(shipping.getId(), shippingHistory.getDestination());
		}

		log.info("배송 도착 처리 완료: shippingHistoryId={}", command.shippingHistoryId());
	}

	private void decreaseOriginHubStock(UUID shippingId, String originHub) {
		List<ProductInfo> productList = getProducts(shippingId);
		UUID hubId = getHubIdFromName(originHub);
		stockClient.decreaseStock(UUID.randomUUID(), hubId, productList);
	}

	private void increaseDestinationHubStock(UUID shippingId, String destinationHub) {
		List<ProductInfo> productList = getProducts(shippingId);
		UUID hubId = getHubIdFromName(destinationHub);
		stockClient.increaseStock(UUID.randomUUID(), hubId, productList);
	}

	private List<ProductInfo> getProducts(UUID shippingId) {
		return orderClient.getProductsByCompanyOrder(shippingId);
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
		return hubNameCache.getOrLoad(hubId);
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


	private List<ShippingHistory> buildHubLegHistories(Shipping shipping, List<Route> routes) {
		return IntStream.range(0, routes.size())
			.mapToObj(i -> {
				Route route = routes.get(i);
				return ShippingHistory.create(
					shipping,
					driverClient.assignHubDriver(),
					i + 1,
					getHubNameFromId(route.departureHubId()),
					getHubNameFromId(route.destinationHubId()),
					route.info()
				);
			})
			.toList();
	}

	private UUID getHubIdFromName(String hubName) {
		return hubIdCache.getOrLoad(hubName);
	}

}