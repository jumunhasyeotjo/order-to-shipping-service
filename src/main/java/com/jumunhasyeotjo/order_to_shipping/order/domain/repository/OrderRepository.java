package com.jumunhasyeotjo.order_to_shipping.order.domain.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsNameRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Optional<Order> findByIdWithAll(UUID id);
    Optional<Order> findById(UUID orderId);
    Order save(Order order);
    Page<Order> findAllByCompanyId(UUID companyId, Pageable pageable);
    boolean existsByIdempotencyKey(String idempotencyKey);
    List<Order> findAllByStatus(OrderStatus orderStatus, Pageable pageable);

    // Internal Query
    List<VendorOrderItemsRes> findAllByVendorOrder(UUID companyOrderId);
    List<VendorOrderItemsNameRes> findAllByVendorOrderName(UUID vendorOrderId);
}
