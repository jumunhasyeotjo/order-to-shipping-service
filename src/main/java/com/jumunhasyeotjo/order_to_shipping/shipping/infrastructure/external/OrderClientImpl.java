package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.OrderClient;

import jakarta.persistence.criteria.Order;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderClientImpl implements OrderClient {
	@Override
	public List<ProductInfo> getProductsByCompanyOrder(UUID companyOrderId) {
		return List.of(new ProductInfo(UUID.randomUUID(),2));
	}
}
