package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OrderFeignConfig {


    // Feign 자체 재시도 비활성화 (Resilience4j가 재시도를 관리하므로 Feign은 재시도하지 않도록 설정)
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    //Feign 로깅 레벨 설정 (필요시 FULL 등으로 변경)
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

     // 5xx 에러 등 일시적 서버 장애를 RetryableException 으로 변환 (Resilience4j 예외 감지하여 재시도 수행 목적)
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * 타임아웃 설정
     * connectTimeout: 5초
     * readTimeout: 5초
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                5000, TimeUnit.MILLISECONDS, // connectTimeout
                5000, TimeUnit.MILLISECONDS, // readTimeout
                true
        );
    }
}
