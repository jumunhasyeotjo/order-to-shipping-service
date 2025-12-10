package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShippingCommand(
	UUID orderProductId,
	UUID orderId,
	LocalDateTime createdAt,
	String productInfo,
	String orderRequest,
	UUID supplierCompanyId,   // 발송(공급) 업체
	UUID receiverCompanyId   // 수령 업체
) {
}