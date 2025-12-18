package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

public record Company(
	UUID companyId,
	String name,
	UUID hubId,
	String address
) {
}