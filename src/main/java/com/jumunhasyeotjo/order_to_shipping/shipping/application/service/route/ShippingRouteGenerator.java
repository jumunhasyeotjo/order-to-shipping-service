package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;

public interface ShippingRouteGenerator {

	List<Route> generateOrRebuildRoute(UUID originHubId, UUID arrivalHubId);
}
