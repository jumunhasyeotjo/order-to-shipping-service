package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.UUID;

public interface CompanyClient {
    boolean existCompany(UUID companyId);
    boolean existCompanyRegionalHub(UUID companyId, UUID hubId);
}
