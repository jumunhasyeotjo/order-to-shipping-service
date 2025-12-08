package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config;

import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderOutboxRepository;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository.JpaOrderOutboxRepository;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository.JpaOrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository.OrderOutboxRepositoryAdapter;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository.OrderRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderRepositoryConfig {

    @Bean
    public OrderRepository orderRepository(JpaOrderRepository jpaOrderRepository) {
        return new OrderRepositoryAdapter(jpaOrderRepository);
    }

    @Bean
    public OrderOutboxRepository orderOutboxRepository(JpaOrderOutboxRepository jpaOrderOutboxRepository) {
        return new OrderOutboxRepositoryAdapter(jpaOrderOutboxRepository);
    }
}
