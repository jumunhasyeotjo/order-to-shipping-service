package com.jumunhasyeotjo.order_to_shipping.payment.domain.vo;

public enum PaymentStatus {

	PAYMENT_PENDING("PAYMENT_PENDING", "결제 승인 전"),
	ABORTED("ABORTED", "결제 실패"),
	CANCELED("CANCELED", "결제 취소"),
	DONE("DONE", "결제 승인"),
	EXPIRED("EXPIRED", "결제 유효시간 만료 (30분)"),
	IN_PROGRESS("IN_PROGRESS", "결제 인증 완료 상태"),
	PARTIAL_CANCELED("PARTIAL_CANCELED", "부분 취소"),
	READY("READY", "결제 초기 단계"),
	WAITING_FOR_DEPOSIT("WAITING_FOR_DEPOSIT", "가상계좌 입금 대기");

	private final String code;        // 시스템/외부 결제 status
	private final String description;

	PaymentStatus(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String code() {
		return code;
	}

	public String description() {
		return description;
	}

	public static PaymentStatus from(String status) {
		return PaymentStatus.valueOf(status.trim().toUpperCase());
	}
}