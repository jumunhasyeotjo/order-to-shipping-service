package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HubClientImpl implements HubClient {
	@Override
	public Optional<String> getHubName(UUID hubId) {
		return Optional.of("name");
	}
}
