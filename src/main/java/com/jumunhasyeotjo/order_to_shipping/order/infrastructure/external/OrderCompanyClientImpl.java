package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderCompanyClientImpl implements OrderCompanyClient {
    @Override
    public boolean existCompany(UUID companyId) {
        return true;
    }

    @Override
    public boolean existCompanyRegionalHub(UUID companyId, UUID hubId) {
        return true;
    }
}
