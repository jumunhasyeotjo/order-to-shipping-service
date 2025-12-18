package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.DriverClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.user.UserServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverClientImpl implements DriverClient {
	private final UserServiceClient userServiceClient;

	@Override
	public Long assignHubDriver() {
		return userServiceClient.getRandomHubDriver();
	}

	@Override
	public Long assignCompanyDriver(UUID hubId) {
		return userServiceClient.getRandomCompanyDriver(hubId);
	}
}
