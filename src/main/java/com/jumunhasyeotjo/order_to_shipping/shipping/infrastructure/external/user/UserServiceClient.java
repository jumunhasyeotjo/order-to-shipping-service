package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
	name = "user-interaction-service"
)
public interface UserServiceClient {

	@GetMapping("/v1/user/internal/hubDriver/random")
	public Long getRandomHubDriver();
}
