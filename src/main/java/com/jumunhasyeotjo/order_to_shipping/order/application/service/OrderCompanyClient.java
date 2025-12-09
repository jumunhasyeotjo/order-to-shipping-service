package com.jumunhasyeotjo.order_to_shipping.order.application.service;


import com.library.passport.entity.ApiRes;

import java.util.UUID;

public interface OrderCompanyClient {
    ApiRes<Boolean> existCompany(UUID companyId);
    ApiRes<Boolean> existCompanyRegionalHub(UUID companyId, UUID hubId);
}
