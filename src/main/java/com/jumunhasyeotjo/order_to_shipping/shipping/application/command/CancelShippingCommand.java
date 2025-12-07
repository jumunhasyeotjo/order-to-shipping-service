package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;

public record CancelShippingCommand(
	UUID shippingId,
	UserRole role,
	String userBelong
) {
}
