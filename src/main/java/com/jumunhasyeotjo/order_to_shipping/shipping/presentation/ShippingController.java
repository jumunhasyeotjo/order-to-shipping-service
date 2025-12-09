package com.jumunhasyeotjo.order_to_shipping.shipping.presentation;

import java.net.URI;
import java.util.List;
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
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
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
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ArriveShippingReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ChangeDriverReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.CreateShippingReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response.ShippingHistoryRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response.ShippingRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.PassportUserRole;
import com.library.passport.proto.PassportProto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.library.passport.proto.PassportProto.Passport;

@Slf4j
@Tag(name = "Shipping", description = "배송 관련 API")
@RestController
@RequestMapping("api/v1/shippings")
@RequiredArgsConstructor
@Validated
public class ShippingController {
	private final ShippingService shippingService;

	@PostMapping
	// @PassportAuthorize(allowedRoles = {PassportUserRole.MASTER})
	@Operation(summary = "배송 생성")
	public ResponseEntity<ApiRes<UUID>> createShipping(
		// @PassportUser Passport passport,
		@Valid @RequestBody CreateShippingReq request
	) {
		log.info("배송 생성 요청: orderProductId={}", request.orderProductId());

		UUID shippingId = shippingService.createShipping(request.toCommand());

		log.info("배송 생성 성공: shippingId={}", shippingId);
		return ResponseEntity.created(URI.create("/api/v1/shippings/" + shippingId)).body(ApiRes.success(shippingId));
	}

	@PatchMapping("/{shippingId}/cancel")
	@PassportAuthorize
	@Operation(summary = "배송 취소")
	public ResponseEntity<ApiRes<UUID>> cancelShipping(
		@PassportUser Passport passport,
		@PathVariable(name = "shippingId") UUID shippingId
	) {
		log.info("배송 취소 요청: shippingId={}", shippingId);

		CancelShippingCommand command = new CancelShippingCommand(
			shippingId,
			UserRole.fromPassportUserRole(PassportUserRole.of(passport.getRole())),
			passport.getBelong()
		);

		shippingService.cancelShipping(command);

		log.info("배송 취소 성공: shippingId={}", shippingId);
		return ResponseEntity.ok(ApiRes.success(shippingId));
	}

	@GetMapping("/{shippingId}")
	@Operation(summary = "배송 조회")
	public ResponseEntity<ApiRes<ShippingRes>> getShipping(
		@PathVariable(name = "shippingId") UUID shippingId
	) {
		log.info("배송 조회 요청: shippingId={}", shippingId);

		GetShippingCommand command = new GetShippingCommand(
			shippingId
		);

		ShippingRes shippingRes = shippingService.getShipping(command);

		log.info("배송 조회 성공: shippingId={}", shippingId);
		return ResponseEntity.ok(ApiRes.success(shippingRes));
	}
}
