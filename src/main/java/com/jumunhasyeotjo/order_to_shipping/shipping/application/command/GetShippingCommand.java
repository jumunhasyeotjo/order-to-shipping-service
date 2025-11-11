package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import java.util.UUID;

public record GetShippingCommand (
	UUID shippingId,
	String role,
	Long userId
){
}
