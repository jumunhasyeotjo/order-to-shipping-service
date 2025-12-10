package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository.ShippingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShippingRepositoryAdapter implements ShippingRepository {
	private final JpaShippingRepository jpaShippingRepository;
	@Override
	public Shipping save(Shipping shipping) {
		return jpaShippingRepository.save(shipping);
	}

	@Override
	public Optional<Shipping> findById(UUID shippingId) {
		return jpaShippingRepository.findById(shippingId);
	}

	@Override
	public List<Shipping> findAllByOrderId(UUID orderId) {
		return jpaShippingRepository.findAllByOrderId(orderId);
	}
}
