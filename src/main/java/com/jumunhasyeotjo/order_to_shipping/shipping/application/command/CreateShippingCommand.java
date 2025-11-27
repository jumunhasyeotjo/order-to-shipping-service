package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.PhoneNumber;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingAddress;

public record CreateShippingCommand(
	UUID orderId,
	PhoneNumber receiverPhoneNumber,
	String receiverName,
	UUID supplierCompanyId,   // 발송(공급) 업체
	UUID receiverCompanyId   // 수령 업체
) {
}