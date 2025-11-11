package com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository;

import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

public interface ShippingHistoryRepository {

	/**
	 * 배송 히스토리 저장
	 */
	List<ShippingHistory> saveAll(List<ShippingHistory> shippingHistories);

	List<ShippingHistory> findAllByShippingId(UUID shippingId);
}
