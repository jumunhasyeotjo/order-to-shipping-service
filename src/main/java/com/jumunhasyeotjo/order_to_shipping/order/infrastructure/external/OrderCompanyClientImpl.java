package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", contextId = "companyClient")
public interface OrderCompanyClientImpl extends OrderCompanyClient {

    @GetMapping("/{companyId}/exists")
    boolean existCompany(@PathVariable UUID companyId);

    @GetMapping("/{companyId}/hub/{hubId}/exist")
    boolean existCompanyRegionalHub(@PathVariable UUID companyId, @PathVariable UUID hubId);
}
