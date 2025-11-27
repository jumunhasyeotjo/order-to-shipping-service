package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeDriverReq(
	@Schema(description = "배송이력 id")
	@NotNull
	UUID shippingHistoryId,
	@Schema(description = "새 배송 담당자 id")
	@NotNull
	Long newDriverId
) {
}
