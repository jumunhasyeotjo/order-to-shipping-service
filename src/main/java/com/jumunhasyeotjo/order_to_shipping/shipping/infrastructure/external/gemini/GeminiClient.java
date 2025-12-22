package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.jumunhasyeotjo.order_to_shipping.common.config.OpenFeignConfig;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiRequest;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiResponse;

@Profile("!perf")
@FeignClient(
	name = "gemini-client",
	url = "${gemini.url}",
	configuration = OpenFeignConfig.class)
public interface GeminiClient {
	@PostMapping("/models/gemini-2.5-flash:generateContent")
	GeminiResponse generateContent(
		@RequestHeader("x-goog-api-key") String apiKey,
		@RequestBody GeminiRequest request
	);
}