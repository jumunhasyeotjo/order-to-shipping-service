package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingHistoryRepository;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShippingHistoryRepositoryAdapter implements ShippingHistoryRepository {
	private final JpaShippingHistoryRepository jpaShippingHistoryRepository;

	@Override
	public List<ShippingHistory> saveAll(List<ShippingHistory> shippingHistories) {
		return jpaShippingHistoryRepository.saveAll(shippingHistories);
	}

	@Override
	public List<ShippingHistory> findAllByShippingId(UUID shippingId) {
		return jpaShippingHistoryRepository.findAllByShippingIdOrderBySequence(shippingId);
	}

	@Override
	public Optional<ShippingHistory> findById(UUID shippingHistoryId) {
		return jpaShippingHistoryRepository.findById(shippingHistoryId);
	}

	@Override
	public Page<ShippingHistory> findAllByDriverId(Long driverId, Pageable pageable) {
		return jpaShippingHistoryRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId, pageable);
	}
}
