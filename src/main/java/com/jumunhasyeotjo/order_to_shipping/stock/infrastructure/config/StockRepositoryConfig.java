package com.jumunhasyeotjo.order_to_shipping.stock.infrastructure.config;

import com.jumunhasyeotjo.order_to_shipping.stock.domain.repository.StockRepository;
import com.jumunhasyeotjo.order_to_shipping.stock.infrastructure.repository.JpaStockRepository;
import com.jumunhasyeotjo.order_to_shipping.stock.infrastructure.repository.StockRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockRepositoryConfig {

    @Bean
    public StockRepository stockRepository(JpaStockRepository jpaStockRepository) {
        return new StockRepositoryAdapter(jpaStockRepository);
    }
}
