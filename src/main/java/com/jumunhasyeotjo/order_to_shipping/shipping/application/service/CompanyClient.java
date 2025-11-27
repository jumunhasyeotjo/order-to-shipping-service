package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;

public interface CompanyClient {

	Company getCompany(UUID companyId);
}
