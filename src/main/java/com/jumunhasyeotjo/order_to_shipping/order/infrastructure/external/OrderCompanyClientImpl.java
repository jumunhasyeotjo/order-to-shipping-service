package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.infrastructure.config.OrderFeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", path = "/internal/api/v1/companies", configuration = OrderFeignConfig.class)
public interface OrderCompanyClientImpl extends OrderCompanyClient {

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackExistCompany")
    @CircuitBreaker(name = "default")
    @GetMapping("/{companyId}/exists")
    ExternalExists existCompany(@PathVariable("companyId") UUID companyId);

    @Retry(name = "externalApiRetry", fallbackMethod = "fallbackExistHub")
    @CircuitBreaker(name = "default")
    @GetMapping("/{companyId}/hub/{hubId}/exist")
    ExternalExists existCompanyRegionalHub(@PathVariable("companyId") UUID companyId, @PathVariable("hubId") UUID hubId);


    default ExternalExists fallbackExistCompany(UUID companyId, Throwable t) {
        System.out.println(t.getMessage());
        System.out.println(t.getStackTrace());
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    default ExternalExists fallbackExistHub(UUID companyId, UUID hubId, Throwable t) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
