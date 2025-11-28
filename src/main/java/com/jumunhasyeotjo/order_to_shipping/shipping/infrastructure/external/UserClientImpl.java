package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserClientImpl implements UserClient {
	@Override
	public void sendSlackMessage(UUID originHubId, UUID receiverCompanyId, String orderIdMessage, String infoMessage,
		String etaMessage, Long driverId) {

	}
}
