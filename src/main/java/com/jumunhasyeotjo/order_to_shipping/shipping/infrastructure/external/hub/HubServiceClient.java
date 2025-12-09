package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.HubResponse;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;
import com.library.passport.entity.ApiRes;

@FeignClient(
    name = "hub-product-stock-company"
)
public interface HubServiceClient {

    @GetMapping("/internal/api/v1/hubs")
    ApiRes<List<RouteResponse>> getRoutes();

    @GetMapping("/internal/api/v1/hubs/routes")
    ApiRes<List<HubResponse>> getAllHubs();
}