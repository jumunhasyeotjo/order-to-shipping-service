package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;

import java.util.UUID;

public interface OrderCompanyClient {
    @Metric(value = "존재하는 업체 검증 (외부)", level = Metric.Level.DEBUG)
    ExternalExists existCompany(UUID companyId);
    ExternalExists existCompanyRegionalHub(UUID companyId, UUID hubId);
}
