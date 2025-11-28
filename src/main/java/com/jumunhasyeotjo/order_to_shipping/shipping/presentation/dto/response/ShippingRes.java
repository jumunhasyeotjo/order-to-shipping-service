package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response;

import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 응답 DTO")
public record ShippingRes(
	@Schema(description = "배송id")
	UUID shippingId,
	@Schema(description = "수령업체 id")
	UUID receiverCompanyId,
	@Schema(description = "배송 상태")
	ShippingStatus shippingStatus,
	@Schema(description = "배송 주소")
	String shippingAddress,
	@Schema(description = "상세 배송 이력")
	List<ShippingHistoryRes> shippingHistories
) {
	public static ShippingRes from(List<ShippingHistory> shippingHistories){
		Shipping shipping = shippingHistories.get(0).getShipping();
		List<ShippingHistoryRes> shippingHistoryResList = shippingHistories.stream().map(ShippingHistoryRes::from).toList();
		return new ShippingRes(
			shipping.getId(),
			shipping.getReceiverCompanyId(),
			shipping.getShippingStatus(),
			shipping.getShippingAddress().getAddress(),
			shippingHistoryResList
		);
	}
}
