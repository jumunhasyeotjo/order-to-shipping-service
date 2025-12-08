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
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.ProcessPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.Money;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.request.ProcessPaymentReq;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.response.PaymentRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.PassportUserRole;
import com.library.passport.proto.PassportProto;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<ApiRes<UUID>> processPayment(
		@RequestBody ProcessPaymentReq request
	) {
		ProcessPaymentCommand command = new ProcessPaymentCommand(
			request.orderId(),
			Money.of(request.amount()),
			request.tossPaymentKey(),
			request.tossOrderId()
		);

		UUID paymentId = paymentService.processPayment(command);
		return ResponseEntity.ok(ApiRes.success(paymentId));
	}

	@GetMapping("/{orderId}")
	@PassportAuthorize(allowedRoles = {PassportUserRole.MASTER, PassportUserRole.COMPANY_MANAGER})
	public ResponseEntity<ApiRes<PaymentRes>> getPaymentInfo(
		@PathVariable UUID orderId,
		@PassportUser PassportProto.Passport passport
	) {
		PaymentRes res = paymentService.getPaymentInfo(orderId);
		return ResponseEntity.ok(ApiRes.success(res));
	}
}

