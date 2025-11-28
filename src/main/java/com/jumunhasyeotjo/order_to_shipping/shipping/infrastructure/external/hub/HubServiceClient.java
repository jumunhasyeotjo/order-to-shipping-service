package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub;

import java.util.List;

import com.jumunhasyeotjo.order_to_shipping.common.ApiRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.HubResponse;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;

@FeignClient(
    name = "hub-product-stock-company", url = "http://localhost:8088" , contextId = "ShippingHubClient"
)
public interface HubServiceClient {

    @GetMapping("/api/v1/hubs/routes")
    ApiRes<List<RouteResponse>> getRoutes();

    @GetMapping("/internal/api/v1/hubs")
    List<HubResponse> getAllHubs();
}