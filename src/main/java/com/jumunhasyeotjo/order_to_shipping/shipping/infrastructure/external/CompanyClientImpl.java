package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CompanyClientImpl implements CompanyClient {
	@Override
	public Company getCompany(UUID companyId) {
		return new Company(UUID.randomUUID(), "업체이름", UUID.randomUUID(), "주소", 1L, RouteInfo.of(1000, 1000));
	}
}