package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.company;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jumunhasyeotjo.order_to_shipping.common.dto.FeignRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.HubResponse;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;
import com.library.passport.entity.ApiRes;

@FeignClient(
    name = "hub-product-stock-company"
)
public interface CompanyServiceClient {

    @GetMapping("/internal/api/v1/companies/{companyId}")
    FeignRes<Company> getCompany(@PathVariable UUID companyId);

}