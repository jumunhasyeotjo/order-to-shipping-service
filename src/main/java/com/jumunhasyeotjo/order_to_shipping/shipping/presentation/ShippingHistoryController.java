package com.jumunhasyeotjo.order_to_shipping.shipping.presentation;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jumunhasyeotjo.order_to_shipping.common.ApiRes;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingHistoryService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.ArriveShippingHistoryCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CancelShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.ChangeDriverCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.DepartShippingHistoryCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetAssignedShippingHistoriesCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ArriveShippingReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ChangeDriverReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.CreateShippingReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response.ShippingHistoryRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response.ShippingRes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Shipping", description = "배송 관련 API")
@RestController
@RequestMapping("api/v1/shipping-histories")
@RequiredArgsConstructor
@Validated
public class ShippingHistoryController {
	private final ShippingHistoryService shippingHistoryService;

	@PatchMapping("/{shippingHistoryId}")
	@Operation(summary = "배송 출발")
	public ResponseEntity<ApiRes<UUID>> departShipping(
		@PathVariable(name = "shippingHistoryId") UUID shippingHistoryId
	) {
		log.info("배송 출발 요청: shippingHistoryId={}", shippingHistoryId);

		DepartShippingHistoryCommand command = new DepartShippingHistoryCommand(
			shippingHistoryId,
			UserRole.HUB_MANAGER,
			1L
		);

		shippingHistoryService.departShippingHistory(command);

		log.info("배송 출발 성공: shippingHistoryId={}", shippingHistoryId);
		return ResponseEntity.ok(ApiRes.success(shippingHistoryId));
	}

	@PatchMapping("/{shippingHistoryId}")
	@Operation(summary = "배송 도착")
	public ResponseEntity<ApiRes<UUID>> arriveShipping(
		@Valid @RequestBody ArriveShippingReq request
	) {
		log.info("배송 도착 요청: shippingHistoryId={}", request.shippingHistoryId());

		ArriveShippingHistoryCommand command = new ArriveShippingHistoryCommand(
			request.shippingHistoryId(),
			UserRole.HUB_MANAGER,
			1L,
			request.actualDistance()
		);

		shippingHistoryService.arriveShippingHistory(command);

		log.info("배송 도착 성공: shippingHistoryId={}", request.shippingHistoryId());
		return ResponseEntity.ok(ApiRes.success(request.shippingHistoryId()));
	}

	@PatchMapping("/{shippingHistoryId}/driver")
	@Operation(summary = "배송자 수정")
	public ResponseEntity<ApiRes<UUID>> changeDriver(
		@Valid @RequestBody ChangeDriverReq request
	) {
		log.info("배송자 수정 요청: shippingHistoryId={}, newDriverId={}", request.shippingHistoryId(), request.newDriverId());

		ChangeDriverCommand command = new ChangeDriverCommand(
			request.shippingHistoryId(),
			UserRole.HUB_MANAGER,
			request.newDriverId()
		);

		shippingHistoryService.changeDriver(command);

		log.info("배송자 수정 성공: shippingHistoryId={}, newDriverId={}", request.shippingHistoryId(), request.newDriverId());
		return ResponseEntity.ok(ApiRes.success(request.shippingHistoryId()));
	}

	@GetMapping("/assigned")
	@Operation(summary = "담당 배송내역 조회 (배송자)")
	public ResponseEntity<ApiRes<Page<ShippingHistoryRes>>> getAssignedShippingHistories(
		@PageableDefault Pageable pageable
	) {
		GetAssignedShippingHistoriesCommand command = new GetAssignedShippingHistoriesCommand(
			1L
		);

		Page<ShippingHistory> shippingHistories = shippingHistoryService.getAssignedShippingHistories(command, pageable);
		Page<ShippingHistoryRes> response = shippingHistories.map(ShippingHistoryRes::from);

		return ResponseEntity.ok(ApiRes.success(response));
	}
}
