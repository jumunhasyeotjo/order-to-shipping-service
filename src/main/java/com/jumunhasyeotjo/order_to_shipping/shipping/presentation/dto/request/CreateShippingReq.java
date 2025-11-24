package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShippingReq(
	@Schema(description = "주문 id", example = "")
	@NotNull
	UUID orderId,
	@Schema(description = "주문자 전화번호", example = "010-1111-2222")
	@NotBlank
	String receiverPhoneNumber,
	@Schema(description = "주문자 이름", example = "홍길동")
	@NotBlank
	String receiverName,

	@Schema(description = "공급 업체")
	@NotNull
	Company supplierCompany,
	@Schema(description = "수령 업체")
	@NotNull
	Company receiverCompany
) {
}
