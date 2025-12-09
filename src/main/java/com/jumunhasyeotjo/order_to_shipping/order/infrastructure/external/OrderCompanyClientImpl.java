package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.library.passport.entity.ApiRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-product-stock-company", path = "/api/v1/companies/internal")
public interface OrderCompanyClientImpl extends OrderCompanyClient {

    @GetMapping("/{companyId}/exists")
    ApiRes<Boolean> existCompany(@PathVariable("companyId") UUID companyId);

    @GetMapping("/{companyId}/hub/{hubId}/exist")
    ApiRes<Boolean> existCompanyRegionalHub(@PathVariable("companyId") UUID companyId, @PathVariable("hubId") UUID hubId);
}
