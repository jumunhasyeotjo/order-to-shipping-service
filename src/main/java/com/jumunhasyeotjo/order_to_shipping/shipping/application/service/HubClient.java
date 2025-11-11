package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.Optional;
import java.util.UUID;

public interface HubClient {
	/**
	 * 허브명 조회
	 * @param hubId
	 * @return
	 */
	Optional<String> getHubName(UUID hubId);

}
