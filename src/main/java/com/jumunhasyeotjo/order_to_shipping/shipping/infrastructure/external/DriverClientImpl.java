package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DriverClientImpl implements DriverClient {
	@Override
	public UUID assignDriver(UUID originHubId, UUID arrivalHubId) {
		return UUID.randomUUID();
	}
}
