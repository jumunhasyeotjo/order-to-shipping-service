package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.service.ShippingDomainService;

@Configuration
public class DomainServiceConfig {
    @Bean
    public ShippingDomainService shippingDomainService() {
        return new ShippingDomainService();
    }
}