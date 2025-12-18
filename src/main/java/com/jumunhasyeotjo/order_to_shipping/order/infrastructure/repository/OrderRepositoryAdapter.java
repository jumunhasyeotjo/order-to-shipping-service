package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsNameRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Optional<Order> findByIdWithAll(UUID id) {
        return jpaOrderRepository.findByIdWithAll(id);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaOrderRepository.findById(orderId);
    }

    @Override
    public Order save(Order order) {return jpaOrderRepository.save(order);}

    @Override
    public Page<Order> findAllByCompanyId(UUID companyId, Pageable pageable) {
        return jpaOrderRepository.findAllByCompanyId(companyId, pageable);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return jpaOrderRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus orderStatus, Pageable pageable) {
        return jpaOrderRepository.findAllByStatus(orderStatus, pageable);
    }

    // Internal Query
    @Override
    public List<VendorOrderItemsRes> findAllByVendorOrder(UUID vendorOrderId) {
        return jpaOrderRepository.findAllByVendorOrder(vendorOrderId);
    }

    @Override
    public List<VendorOrderItemsNameRes> findAllByVendorOrderName(UUID vendorOrderId) {
        return jpaOrderRepository.findAllByVendorOrderName(vendorOrderId);
    }

}
