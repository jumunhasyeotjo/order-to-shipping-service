package com.jumunhasyeotjo.order_to_shipping.stock.application;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.stock.domain.entity.Stock;
import com.jumunhasyeotjo.order_to_shipping.stock.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void decreaseStock(UUID orderId, List<OrderProduct> orderProducts) {
        for (OrderProduct orderProduct : orderProducts) {
            Stock stock = stockRepository.findByProductId(orderProduct.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));

            stock.decreaseStock(orderProduct.getQuantity());
            stockRepository.save(stock);

        }
    }
}
