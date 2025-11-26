package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.CreateShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShippingReq(
	@Schema(description = "주문 업체 id", example = "")
	@NotNull
	UUID orderProductId,

	@Schema(description = "주문자 전화번호", example = "010-1111-2222")
	@NotBlank
	String receiverPhoneNumber,

	@Schema(description = "주문 시간", example = "2025-12-08 10:00:00")
	@NotNull
	LocalDateTime createdAt,

	@Schema(description = "상품 정보", example = "마른 오징어 50박스")
	@NotBlank
	String productInfo,

	@Schema(description = "요청 사항", example = "12월 12일 3시까지는 보내주세요!")
	@NotBlank
	String orderRequest,

	@Schema(description = "주문자 이름", example = "홍길동")
	@NotBlank
	String receiverName,

	@Schema(description = "공급 업체 id")
	@NotNull
	UUID supplierCompanyId,

	@Schema(description = "수령 업체 id")
	@NotNull
	UUID receiverCompanyId
) {
	public CreateShippingCommand toCommand(){
		return new CreateShippingCommand(
			this.orderProductId,
			PhoneNumber.of(this.receiverPhoneNumber),
			this.receiverName,
			this.createdAt,
			this.productInfo,
			this.orderRequest,
			this.supplierCompanyId,
			this.receiverCompanyId
		);
	}
}