package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config.OrderFeignConfig;
import com.library.passport.entity.ApiRes;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", path = "/api/v1/companies/internal", configuration = OrderFeignConfig.class)
public interface OrderCompanyClientImpl extends OrderCompanyClient {

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackExistCompany")
    @CircuitBreaker(name = "default")
    @GetMapping("/{companyId}/exists")
    ApiRes<Boolean> existCompany(@PathVariable("companyId") UUID companyId);

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackExistHub")
    @CircuitBreaker(name = "default")
    @GetMapping("/{companyId}/hub/{hubId}/exist")
    ApiRes<Boolean> existCompanyRegionalHub(@PathVariable("companyId") UUID companyId, @PathVariable("hubId") UUID hubId);

    // Fallback: 업체 존재 여부 확인 실패
    default ApiRes<Boolean> fallbackExistCompany(UUID companyId, Throwable t) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // Fallback: 허브 존재 여부 확인 실패
    default ApiRes<Boolean> fallbackExistHub(UUID companyId, UUID hubId, Throwable t) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
