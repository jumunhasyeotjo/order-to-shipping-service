package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.ShippingStockProduct;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock.StockServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingStockClientImpl implements StockClient {
	private final StockServiceClient stockServiceClient;
	@Override
	public void increaseStock(UUID idempotencyKey, UUID hubId, List<ProductInfo> productList) {
		List<ShippingStockProduct> productReqs = productList.stream().map(
			productInfo -> ShippingStockProduct.from(productInfo, hubId)
		).toList();

		stockServiceClient.incrementStock(productReqs, idempotencyKey);
	}

	@Override
	public void decreaseStock(UUID idempotencyKey, UUID hubId, List<ProductInfo> productList) {
		List<ShippingStockProduct> productReqs = productList.stream().map(
			productInfo -> ShippingStockProduct.from(productInfo, hubId)
		).toList();

		stockServiceClient.decreaseStock(productReqs, idempotencyKey);
	}
}
