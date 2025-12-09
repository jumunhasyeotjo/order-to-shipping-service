package com.jumunhasyeotjo.order_to_shipping.shipping.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;

public interface ShippingRepository {
	/**
	 * 배송 생성
	 * @param shipping
	 * @return
	 */
	Shipping save(Shipping shipping);

	Optional<Shipping> findById(UUID shippingId);



}
