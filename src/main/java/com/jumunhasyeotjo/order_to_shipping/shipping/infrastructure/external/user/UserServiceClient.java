package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.user;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.library.passport.entity.ApiRes;

@FeignClient(
	name = "user-service"
)
public interface UserServiceClient {

	@GetMapping("/internal/api/v1/users/hub-driver/random")
	Long getRandomHubDriver();

	@GetMapping("/internal/api/v1/users/company-driver/random/{hubId}")
	Long getRandomCompanyDriver(@PathVariable UUID hubId);
}
