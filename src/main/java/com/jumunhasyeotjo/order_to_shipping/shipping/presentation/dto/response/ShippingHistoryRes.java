package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public record ShippingHistoryRes(
	@Schema(description = "상세 배송 이력 id")
	UUID shippingHistoryId,
	@Schema(description = "배송자 id")
	Long driverId, //todo 이름으로 교체?
	@Schema(description = "배송 순번")
	Integer sequence,
	@Schema(description = "출발지")
	String origin,
	@Schema(description = "도착지")
	String destination,
	@Schema(description = "배송 상태")
	ShippingHistoryStatus status,
	@Schema(description = "실제 배송 정보(시간/거리)")
	RouteInfo actualRouteInfo,
	@Schema(description = "출발 시간")
	LocalDateTime departedAt,
	@Schema(description = "도착 시간")
	LocalDateTime arrivedAt
) {
	public static ShippingHistoryRes from(ShippingHistory shippingHistory){
		return new ShippingHistoryRes(shippingHistory.getId(),
			shippingHistory.getDriverId(),
			shippingHistory.getSequence(),
			shippingHistory.getOrigin(),
			shippingHistory.getDestination(),
			shippingHistory.getStatus(),
			shippingHistory.getActualRouteInfo(),
			shippingHistory.getDepartedAt(),
			shippingHistory.getArrivedAt());
	}
}
