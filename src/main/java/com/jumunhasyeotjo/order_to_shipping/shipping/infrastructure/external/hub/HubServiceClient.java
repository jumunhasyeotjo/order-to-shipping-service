package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.HubResponse;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;

@FeignClient(
    name = "hub-product-stock-company"
)
public interface HubServiceClient {

    @GetMapping("/internal/api/v1/hubs")
    List<RouteResponse> getRoutes();

    @GetMapping("/internal/api/v1/hubs/routes")
    List<HubResponse> getAllHubs();
}