package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.List;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.HubInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;

public interface HubClient {
	List<Route> getRoutes();

	List<HubInfo> getAllHubs();

}
