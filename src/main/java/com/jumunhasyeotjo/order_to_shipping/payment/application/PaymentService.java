package com.jumunhasyeotjo.order_to_shipping.payment.application;

import static org.springframework.transaction.annotation.Propagation.*;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.payment.application.command.ProcessPaymentCommand;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.PaymentPgRaw;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentPgRawRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossConfirmRequest;
import com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto.TossPaymentResponse;
import com.jumunhasyeotjo.order_to_shipping.payment.presentation.dto.response.PaymentRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.OrderClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final TossPaymentService tossPaymentService;
	private final PaymentPgRawRepository paymentPgRawRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Transactional
	public UUID processPayment(ProcessPaymentCommand command) {
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

	@Transactional(propagation = REQUIRES_NEW)
	public void confirmPayment(Payment payment) {
		TossConfirmRequest req = buildTossConfirm(payment);
		try {
			TossPaymentResponse res = tossPaymentService.confirm(req);
			payment.setPaymentResult(res.getStatus(), res.getMethod(), res.getApprovedAt());
			PaymentPgRaw paymentPgRaw = new PaymentPgRaw(payment.getId(), resToJson(res));
			paymentPgRawRepository.save(paymentPgRaw);
		} catch (BusinessException e) {
			log.error("PG 결제 확정 실패. orderId={}", payment.getOrderId(), e);
			payment.failPayment();
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public PaymentRes getPaymentInfo(UUID orderId) {
		Payment payment = getPaymentByOrderId(orderId);
		return PaymentRes.of(tossPaymentService.getPaymentInfo(payment.getTossPaymentKey()), payment.getId());
	}

	private Payment getPaymentByOrderId(UUID orderId){
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

	private String resToJson(TossPaymentResponse res) {
		try {
			return objectMapper.writeValueAsString(res);
		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
