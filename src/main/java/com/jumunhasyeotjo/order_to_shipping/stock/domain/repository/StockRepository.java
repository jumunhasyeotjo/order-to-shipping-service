package com.jumunhasyeotjo.order_to_shipping.stock.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.stock.domain.entity.Stock;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository {
    Optional<Stock> findByProductId(UUID id);
    Stock save(Stock stock);
    void deleteAll();
}
