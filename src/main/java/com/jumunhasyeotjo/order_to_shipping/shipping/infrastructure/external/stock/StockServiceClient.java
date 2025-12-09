package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderStockClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.hub.dto.RouteResponse;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.stock.dto.StockResponse;
import com.library.passport.entity.ApiRes;

@FeignClient(
	name = "hub-product-stock-company", path = "/internal/api/v1/stocks/shipped"
)
public interface StockServiceClient {
	@PostMapping("/decrement")
	ApiRes<StockResponse> decreaseStock(List<OrderProductReq> productList, UUID hubId, @RequestHeader("Idempotency-Key") UUID idempotencyKey);

	@PostMapping("/increment")
	ApiRes<StockResponse> incrementStock(List<OrderProductReq> productList, UUID hubId, @RequestHeader("Idempotency-Key") UUID idempotencyKey);
}