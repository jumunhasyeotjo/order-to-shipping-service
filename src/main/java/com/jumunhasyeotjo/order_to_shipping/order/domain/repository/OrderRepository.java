package com.jumunhasyeotjo.order_to_shipping.order.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Optional<Order> findById(UUID id);
    Order save(Order order);
    List<Order> findAllByCompanyId(UUID companyId, Pageable pageable);
}
