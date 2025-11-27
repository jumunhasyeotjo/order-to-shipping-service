package com.jumunhasyeotjo.order_to_shipping.config;

import com.jumunhasyeotjo.order_to_shipping.common.service.RedisService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    public RedisService redisService() {
        return Mockito.mock(RedisService.class);
    }
}