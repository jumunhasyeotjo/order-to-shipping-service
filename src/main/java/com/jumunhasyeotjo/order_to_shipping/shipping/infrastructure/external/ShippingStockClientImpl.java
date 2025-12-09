package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.StockClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShippingStockClientImpl implements StockClient {
	@Override
	public void increaseStock(UUID idempotencyKey, UUID hubId, List<ProductInfo> productList) {
	}

	@Override
	public void decreaseStock(UUID idempotencyKey, UUID hubId, List<ProductInfo> productList) {
	}
}
