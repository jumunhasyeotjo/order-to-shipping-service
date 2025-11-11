package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
		return jpaShippingHistoryRepository.findAllByShippingId(shippingId);
	}
}
