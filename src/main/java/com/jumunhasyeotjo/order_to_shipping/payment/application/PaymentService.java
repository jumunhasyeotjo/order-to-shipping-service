package com.jumunhasyeotjo.order_to_shipping.payment.application;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.util.JsonUtil;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.CancelPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.ProcessPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.PaymentPgRaw;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentPgRawRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossConfirmRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossPaymentResponse;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.response.PaymentRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final TossPaymentService tossPaymentService;
	private final PaymentPgRawRepository paymentPgRawRepository;

	private final JsonUtil jsonUtil;

	@Metric("결제 처리 전체 프로세스")
	@Transactional(noRollbackFor = BusinessException.class)
	public UUID processPayment(ProcessPaymentCommand command) {
		validatePaymentKey(command.tossPaymentKey());

		Payment newPayment = Payment.builder()
			.orderId(command.orderId())
			.amount(command.amount())
			.tossPaymentKey(command.tossPaymentKey())
			.tossOrderId(command.tossOrderId())
			.build();

		paymentRepository.save(newPayment);

		confirmPayment(newPayment);

		return newPayment.getOrderId();
	}

	private void confirmPayment(Payment payment) {
		TossConfirmRequest req = buildTossConfirm(payment);
		try {
			TossPaymentResponse res = tossPaymentService.confirm(req, payment);

			payment.setPaymentResult(res.getStatus(), res.getMethod(), res.getApprovedAt());
			savePgRaw(res, payment.getId());

		} catch (BusinessException e) {
			payment.failPayment("PAYMENT_CONFIRM_ERROR", "서버에서 결제 처리 중 오류가 발생했습니다.");

			log.error("PG 결제 확정 실패. orderId={}", payment.getOrderId(), e);
			throw e;
		}
	}

	@Transactional(noRollbackFor = BusinessException.class)
	public void cancelPayment(CancelPaymentCommand command) {
		Payment payment = getPaymentByOrderId(command.orderId());
		try {
			TossPaymentResponse res = tossPaymentService.cancel(command.cancelReason(), payment);
			updatePgRaw(res, payment.getId());

		} catch (BusinessException e) {
			payment.failPayment("PAYMENT_CONFIRM_ERROR", "서버에서 결제 처리 중 오류가 발생했습니다.");

			log.error("PG 결제 취소 실패. orderId={}", payment.getOrderId(), e);
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public PaymentRes getPaymentInfo(UUID orderId) {
		Payment payment = getPaymentByOrderId(orderId);
		return PaymentRes.of(tossPaymentService.getPaymentInfo(payment.getTossPaymentKey()), payment.getId());
	}

	@Transactional(readOnly = true)
	public String getPaymentDetailInfo(UUID paymentId) {
		PaymentPgRaw pgRaw = getPaymentPgRawByPaymentId(paymentId);

		return pgRaw.getPgResponseJson();
	}

	private void savePgRaw(TossPaymentResponse res, UUID paymentId) {
		PaymentPgRaw paymentPgRaw = new PaymentPgRaw(paymentId, jsonUtil.toJson(res));
		paymentPgRawRepository.save(paymentPgRaw);
	}

	private void updatePgRaw(TossPaymentResponse res, UUID paymentId) {
		PaymentPgRaw raw = getPaymentPgRawByPaymentId(paymentId);
		raw.updatePgResponseJson(jsonUtil.toJson(res));
	}

	private void validatePaymentKey(String tossPaymentKey){
		if(paymentRepository.existsByTossPaymentKey(tossPaymentKey)) {
			throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_INTENT);
		}
	}

	private Payment getPaymentByOrderId(UUID orderId) {
		return paymentRepository.findByOrderId(orderId).orElseThrow(
			() -> new BusinessException(ErrorCode.NOT_FOUND_BY_ID)
		);
	}

	private TossConfirmRequest buildTossConfirm(Payment payment) {
		return TossConfirmRequest.builder()
			.orderId(payment.getTossOrderId())
			.paymentKey(payment.getTossPaymentKey())
			.amount(payment.getAmount().getAmount())
			.build();
	}

	private PaymentPgRaw getPaymentPgRawByPaymentId(UUID paymentId){
		return paymentPgRawRepository.findByPaymentId(paymentId).orElseThrow(
			() -> new BusinessException(ErrorCode.NOT_FOUND_BY_ID)
		);
	}
}
