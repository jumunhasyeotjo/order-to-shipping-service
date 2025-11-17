package com.jumunhasyeotjo.order_to_shipping.stock.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.stock.domain.entity.Stock;
import com.jumunhasyeotjo.order_to_shipping.stock.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepository {

    private final JpaStockRepository jpaStockRepository;

    @Override
    public Optional<Stock> findByProductId(UUID productId) {
        return jpaStockRepository.findByProductId(productId);
    }

    @Override
    public Stock save(Stock stock) {
        return jpaStockRepository.save(stock);
    }

    @Override
    public void deleteAll() {
        jpaStockRepository.deleteAll();
    }
}
