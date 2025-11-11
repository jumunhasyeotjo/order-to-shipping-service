package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

@Repository
public interface JpaShippingHistoryRepository extends JpaRepository<ShippingHistory, UUID> {

	List<ShippingHistory> findAllByShippingId(UUID shippingId);
}
