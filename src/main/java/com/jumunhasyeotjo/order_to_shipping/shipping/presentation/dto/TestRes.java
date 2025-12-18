package com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;

public record TestRes(
	List<Route> routeList,
	Set<UUID> hubIds
) {
}
