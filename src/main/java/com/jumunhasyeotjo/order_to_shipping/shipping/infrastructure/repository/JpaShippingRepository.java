package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;

@Repository
public interface JpaShippingRepository extends JpaRepository<Shipping, UUID> {
	List<Shipping> findAllByOrderId(UUID orderId);
}
