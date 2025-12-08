package com.jumunhasyeotjo.order_to_shipping.payment.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jumunhasyeotjo.order_to_shipping.common.ApiRes;
import com.jumunhasyeotjo.order_to_shipping.payment.application.PaymentService;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.CancelPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.ProcessPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.Money;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request.CancelPaymentReq;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request.ProcessPaymentReq;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.response.PaymentRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.PassportUserRole;
import com.library.passport.proto.PassportProto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@Operation(summary = "결제 요청")
	public ResponseEntity<ApiRes<UUID>> processPayment(
		@RequestBody ProcessPaymentReq request
	) {
		ProcessPaymentCommand command = new ProcessPaymentCommand(
			request.orderId(),
			request.amount(),
			request.tossPaymentKey(),
			request.tossOrderId()
		);

		UUID paymentId = paymentService.processPayment(command);
		return ResponseEntity.ok(ApiRes.success(paymentId));
	}

	@PostMapping("/{paymentId}/cancel")
	@Operation(summary = "결제 취소 요청")
	public ResponseEntity<ApiRes<UUID>> cancelPayment(
		@PathVariable UUID paymentId,
		@RequestBody CancelPaymentReq request
	) {
		CancelPaymentCommand command = new CancelPaymentCommand(
			paymentId,
			request.cancelReason()
		);

		paymentService.cancelPayment(command);
		return ResponseEntity.ok(ApiRes.success(paymentId));
	}

	@GetMapping("/{orderId}")
	@Operation(summary = "결제 정보 조회")
	@PassportAuthorize(allowedRoles = {PassportUserRole.MASTER, PassportUserRole.COMPANY_MANAGER})
	public ResponseEntity<ApiRes<PaymentRes>> getPaymentInfo(
		@PathVariable UUID orderId,
		@PassportUser PassportProto.Passport passport
	) {
		PaymentRes res = paymentService.getPaymentInfo(orderId);
		return ResponseEntity.ok(ApiRes.success(res));
	}

	@GetMapping("/{paymentId}/detail")
	@Operation(summary = "결제 PG 정보 상세 조회")
	@PassportAuthorize(allowedRoles = {PassportUserRole.MASTER, PassportUserRole.COMPANY_MANAGER})
	public ResponseEntity<ApiRes<String>> getPaymentDetailInfo(
		@PathVariable UUID paymentId,
		@PassportUser PassportProto.Passport passport
	) {
		String res = paymentService.getPaymentDetailInfo(paymentId);
		return ResponseEntity.ok(ApiRes.success(res));
	}
}

