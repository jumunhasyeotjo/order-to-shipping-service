package com.jumunhasyeotjo.order_to_shipping.common.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 작업 실패 - Method: {}, Exception: {}",
                method.getName(), ex.getMessage());

            // todo
            // 1. 모니터링 시스템에 알림 발송
            // 2. 실패한 작업을 DB에 저장 (나중에 재처리)
            // 3. 관리자에게 이메일 발송
        };
    }
}
