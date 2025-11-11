package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

import java.time.LocalDateTime;
import java.time.Duration;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteInfo {

	private Integer distance;

	private Integer interval;

	public static RouteInfo of(Integer distance, Integer interval){
		return new RouteInfo(distance, interval);
	}
	public static RouteInfo ofActualRoute(Integer distance, LocalDateTime departedAt, LocalDateTime arrivedAt) {
		Integer interval = (int) Duration.between(departedAt, arrivedAt).toMinutes();
		System.out.println(interval);
		return new RouteInfo(distance, interval);
	}

	public RouteInfo(Integer distance, Integer interval) {
		if (distance == null || distance <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (interval == null || interval <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		this.distance = distance;
		this.interval = interval;
	}
}
