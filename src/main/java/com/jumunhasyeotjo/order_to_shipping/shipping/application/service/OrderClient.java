package com.jumunhasyeotjo.order_to_shipping.shipping.application.service;

import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfoName;

public interface OrderClient {
	List<ProductInfo> getProductsByCompanyOrder(UUID companyOrderId);

    List<ProductInfoName> getProductsByVendorOrderNameAndQuantity(UUID id);
}
