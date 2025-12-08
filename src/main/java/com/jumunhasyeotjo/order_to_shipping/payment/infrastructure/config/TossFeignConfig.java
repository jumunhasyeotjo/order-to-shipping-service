package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

public class TossFeignConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor tossAuthInterceptor() {
        return template -> {
            String token = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + token);
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");
        };
    }

    @Bean
    public ErrorDecoder tossErrorDecoder() {
        return new TossFeignErrorDecoder();
    }
}