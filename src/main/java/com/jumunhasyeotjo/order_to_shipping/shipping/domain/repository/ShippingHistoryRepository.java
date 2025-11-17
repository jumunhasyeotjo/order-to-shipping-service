package com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

public interface ShippingHistoryRepository {

	/**
	 * 배송 히스토리 저장
	 */
	List<ShippingHistory> saveAll(List<ShippingHistory> shippingHistories);

	List<ShippingHistory> findAllByShippingId(UUID shippingId);

	Optional<ShippingHistory> findById(UUID shippingHistoryId);

	Page<ShippingHistory> findByDriverId(Long driverId, Pageable pageable);
}
