package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaShippingHistoryRepository extends JpaRepository<ShippingHistory, UUID> {

	List<ShippingHistory> findAllByShippingIdOrderBySequence(UUID shippingId);

	Page<ShippingHistory> findAllByDriverIdOrderByCreatedAtDesc(Long driverId, Pageable pageable);

    Optional<ShippingHistory> findByShippingId(UUID shippingId);
}
