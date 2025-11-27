package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.UUID;

public interface OrderCompanyClient {
    boolean existCompany(UUID companyId);
    boolean existCompanyRegionalHub(UUID companyId, UUID hubId);
}
