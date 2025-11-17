package com.jumunhasyeotjo.order_to_shipping.order.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Optional<Order> findById(UUID id);
    Order save(Order order);
    Page<Order> findAllByCompanyId(UUID companyId, Pageable pageable);
    void deleteAll();
}
