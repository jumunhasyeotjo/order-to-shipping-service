package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // 5xx 에러인 경우 일시적 서버 장애로 판단하여 재시도 유도
        if (response.status() >= 500) {
            log.error("Feign Server Error: status={}, methodKey={}", response.status(), methodKey);
            return new RetryableException(
                response.status(),
                "Server Error",
                response.request().httpMethod(),
                    (Long) null,
                response.request()
            );
        }
        
        // 그 외(4xx 등)는 기본 디코더 사용 (재시도 안 함)
        return defaultDecoder.decode(methodKey, response);
    }
}