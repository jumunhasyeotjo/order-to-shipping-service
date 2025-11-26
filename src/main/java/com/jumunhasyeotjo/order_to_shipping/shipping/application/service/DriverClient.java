package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

public interface DriverClient {
	/**
	 * 배송 기사 배정
	 * @param originHubId
	 * @param arrivalHubId
	 * @return
	 */
	Long assignDriver(UUID originHubId, UUID arrivalHubId);
}
