package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.jumunhasyeotjo.order_to_shipping.common.dto.FeignRes;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.ShippingStockProduct;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock.dto.StockResponse;
import com.library.passport.entity.ApiRes;

@FeignClient(
	name = "hub-product-stock-company", path = "/internal/api/v1/stocks/shipped"
)
public interface StockServiceClient {
	@PostMapping("/decrement")
	FeignRes<StockResponse> decreaseStock(List<ShippingStockProduct> productList, @RequestHeader("Idempotency-Key") UUID idempotencyKey);

	@PostMapping("/increment")
	FeignRes<StockResponse> incrementStock(List<ShippingStockProduct> productList, @RequestHeader("Idempotency-Key") UUID idempotencyKey);
}