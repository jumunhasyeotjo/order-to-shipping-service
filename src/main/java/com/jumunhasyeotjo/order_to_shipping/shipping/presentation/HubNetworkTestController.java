package com.jumunhasyeotjo.order_to_shipping.shipping.presentation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubNetwork;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.TestRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.response.ShippingRes;
import com.library.passport.entity.ApiRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/shippings/test")
@RequiredArgsConstructor
public class HubNetworkTestController {

    private final HubNetwork hubNetwork;
    private final HubClient hubClient;

    @GetMapping
    public ResponseEntity<ApiRes<TestRes>> testLoadRoutes() {
        List<Route> routeList = hubClient.getRoutes();
        Set<UUID> ids =  hubNetwork.hubIds();

        return ResponseEntity.ok(ApiRes.success(new TestRes(routeList, ids)));
    }
}