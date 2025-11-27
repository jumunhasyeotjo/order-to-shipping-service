package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.HubInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;

public interface HubClient {
	/**
	 * 허브명 조회
	 * @param hubId
	 * @return
	 */
	Optional<String> getHubName(UUID hubId);

	List<Route> getRoutes();

	List<HubInfo> getAllHubNames();

}
