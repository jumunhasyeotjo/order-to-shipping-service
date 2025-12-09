package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

public interface DriverClient {
	/**
	 * 배송 기사 배정
	 * @param originHubId
	 * @param arrivalHubId
	 * @return
	 */
	Long assignDriver(UUID originHubId, UUID arrivalHubId);
}