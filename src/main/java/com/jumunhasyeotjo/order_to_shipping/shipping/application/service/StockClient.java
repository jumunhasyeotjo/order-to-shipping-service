package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;

public interface StockClient {
	void increaseStock(UUID hubId, List<ProductInfo> productList);
	void decreaseStock(UUID hubId, List<ProductInfo> productList);
}
