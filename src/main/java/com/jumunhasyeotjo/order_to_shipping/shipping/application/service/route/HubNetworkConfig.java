package com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.Route;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class HubNetworkConfig {

    private final HubClient hubClient;

    @Bean
    HubNetwork hubNetwork() {
        List<Route> routes = hubClient.getRoutes();
        return new RouteBasedHubNetwork(routes);
    }
}