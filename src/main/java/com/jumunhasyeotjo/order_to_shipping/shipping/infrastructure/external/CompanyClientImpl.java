package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.Company;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.company.CompanyServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyClientImpl implements CompanyClient {
	private final CompanyServiceClient companyServiceClient;
	@Override
	public Company getCompany(UUID companyId) {
		return companyServiceClient.getCompany(companyId).getData();
	}
}