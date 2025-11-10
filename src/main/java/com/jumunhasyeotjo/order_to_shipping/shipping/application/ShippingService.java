package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CancelShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
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
	private final ShippingHistoryRepository shippingHistoryRepository;

	private final HubClient hubClient;
	private final DriverClient driverClient;

	@Transactional
	public UUID createShipping(CreateShippingCommand command) {
		log.info("배송 생성 시작: orderId={}", command.orderId());
		UUID originHubId = command.supplierCompany().hubId();
		UUID arrivalHubId = command.receiverCompany().hubId();

		// 경로 생성
		List<Route> routes = shippingRouteGenerator.generatorRoute(originHubId, arrivalHubId);

		// 배송 생성
		Shipping shipping = Shipping.create(command.orderId(), command.receiverCompany().companyId(),
			ShippingAddress.of(command.receiverCompany().address()),
			command.receiverPhoneNumber(), command.receiverName(), command.supplierCompany().hubId(),
			command.receiverCompany().hubId(), routes.size());

		// 배송 이력 생성
		List<ShippingHistory> shippingHistories = createShippingHistoryList(shipping, routes, command.receiverCompany());

		shippingRepository.save(shipping);
		shippingHistoryRepository.saveAll(shippingHistories);

		// todo 슬랙 메시지 보내기

		log.info("배송 생성 완료: shippingId={}", shipping.getId());
		return shipping.getId();
	}

	/**
	 * 배송 취소
	 * @param command
	 */
	@Transactional
	public void cancelShipping(CancelShippingCommand command){
		validateCancellableBy(command.role(),command.userId());
		Shipping shipping = getShippingById(command.shippingId());
		List<ShippingHistory> shippingHistories = getShippingHistoryList(command.shippingId());

		shippingDomainService.cancelDelivery(shipping, shippingHistories);
	}

	/**
	 * 배송 조회
	 */
	@Transactional(readOnly = true)
	public ShippingResult getShipping(GetShippingCommand command) {
		validateViewableBy(command.role(),command.userId());
		Shipping shipping = getShippingById(command.shippingId());
		List<ShippingHistory> shippingHistories = getShippingHistoryList(command.shippingId());

		return ShippingResult.from(shipping, shippingHistories);
	}

	private void validateCancellableBy(String role, Long userId){
		//todo 검증로직 추후 추가
	}

	private void validateViewableBy(String role, Long userId){
		//todo 검증로직 추후 추가
	}

	private Shipping getShippingById(UUID shippingId){
		return shippingRepository.findById(shippingId).orElseThrow(
			() -> new BusinessException(NOT_FOUND_BY_ID)
		);
	}


	private List<ShippingHistory> getShippingHistoryList(UUID shippingId){
		List<ShippingHistory> shippingHistories = shippingHistoryRepository.findAllByShippingId(shippingId);
		if(shippingHistories.isEmpty())
			throw new BusinessException(NOT_FOUND_BY_ID);

		return shippingHistories;
	}

	private List<ShippingHistory> createShippingHistoryList(Shipping shipping, List<Route> routes, Company recevierCompany) {
		List<ShippingHistory> hubLegHistories =
			IntStream.range(0, routes.size())
				.mapToObj(i -> {
					Route route = routes.get(i);
					String departureName = getHubNameFromId(route.departureHubId());
					String destinationName = getHubNameFromId(route.destinationHubId());
					return ShippingHistory.create(
						shipping,
						driverClient.assignDriver(route.departureHubId(), route.destinationHubId()),
						i + 1,
						departureName,
						destinationName,
						route.info()
					);
				})
				.toList();

		ShippingHistory finalHistory = ShippingHistory.create(
			shipping,
			recevierCompany.driverId(),
			hubLegHistories.size() + 1,
			getHubNameFromId(recevierCompany.hubId()),
			recevierCompany.companyName(),
			recevierCompany.routeInfo()
		);

		return Stream.concat(hubLegHistories.stream(), Stream.of(finalHistory)).toList();
	}

	private String getHubNameFromId(UUID hubId){
		return hubClient.getHubName(hubId).orElseThrow(() -> new BusinessException(NOT_FOUND_BY_ID));
	}

}
