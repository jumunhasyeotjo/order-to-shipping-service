package com.jumunhasyeotjo.order_to_shipping.stock.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.stock.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaStockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByProductId(UUID productId);
}
