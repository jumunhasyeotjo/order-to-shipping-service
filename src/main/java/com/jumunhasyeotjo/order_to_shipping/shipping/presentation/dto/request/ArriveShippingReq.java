package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ArriveShippingReq(
	@Schema(description = "배송이력 id")
	@NotNull
	UUID shippingHistoryId,
	@Schema(description = "실제 배송 거리")
	@NotNull
	Integer actualDistance
) {
}
