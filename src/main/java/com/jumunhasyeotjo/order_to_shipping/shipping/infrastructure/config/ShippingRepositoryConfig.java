package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository.JpaShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository.JpaShippingRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository.ShippingHistoryRepositoryAdapter;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository.ShippingRepositoryAdapter;

@Configuration
public class ShippingRepositoryConfig {
    @Bean
    public ShippingRepository shippingRepository(JpaShippingRepository jpaShippingRepository) {
        return new ShippingRepositoryAdapter(jpaShippingRepository);
    }

    @Bean
    public ShippingHistoryRepository shippingHistoryRepository(JpaShippingHistoryRepository jpaShippingHistoryRepository) {
        return new ShippingHistoryRepositoryAdapter(jpaShippingHistoryRepository);
    }
}
