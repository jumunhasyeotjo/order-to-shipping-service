package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

public interface UserClient {

	void sendSlackMessage(UUID originHubId, UUID receiverCompanyId, String orderIdMessage, String infoMessage, String etaMessage, Long driverId);
}
