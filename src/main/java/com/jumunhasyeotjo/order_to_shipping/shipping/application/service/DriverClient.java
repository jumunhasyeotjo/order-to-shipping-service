package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

public interface DriverClient {
	Long assignHubDriver();
	Long assignCompanyDriver(UUID hubId);
}