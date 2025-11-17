package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

public interface UserClient {
	boolean isManagingHub(Long userId, UUID hubId);
}
