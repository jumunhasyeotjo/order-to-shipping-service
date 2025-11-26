package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.UserClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserClientImpl implements UserClient {
	@Override
	public boolean isManagingHub(Long userId, UUID hubId) {
		return true;
	}
}
