package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ExternalExists;

import java.util.UUID;

public interface OrderCompanyClient {
    ExternalExists existCompany(UUID companyId);
    ExternalExists existCompanyRegionalHub(UUID companyId, UUID hubId);
}
