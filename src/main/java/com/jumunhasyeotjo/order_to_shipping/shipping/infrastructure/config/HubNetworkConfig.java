package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.HubNetwork;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.RouteBasedHubNetwork;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
class HubNetworkConfig {

    private final HubClient hubClient;

    @Bean
    @Lazy
    HubNetwork hubNetwork() {
        try {
            log.info("허브 경로 요청");
            List<Route> routes = hubClient.getRoutes();
            log.info("허브 경로 요청 성공 {}개", routes.size());

            return new RouteBasedHubNetwork(routes);
        } catch (Exception e) {
            log.warn("Failed to load hub routes from HubClient. Falling back to empty hub network.", e);
            return new RouteBasedHubNetwork(Collections.emptyList());
        }
    }
}