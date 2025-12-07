package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CompanyOrderItemsRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<Order, UUID>{

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderCompanies oc " +
            "JOIN FETCH oc.orderProducts " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAll(@Param("orderId") UUID orderId);

    @Query(value = "select distinct o from Order o " +
            "where o.receiverCompanyId = :companyId",
            countQuery = "select count(o) from Order o where o.receiverCompanyId = :companyId")
    Page<Order> findAllByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT new com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CompanyOrderItemsRes(op.productId, op.quantity) " +
            "FROM OrderProduct op " +
            "WHERE op.orderCompany.id = :companyOrderId")
    List<CompanyOrderItemsRes> findAllByOrderCompany(UUID companyOrderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Order> findAllByStatus(OrderStatus orderStatus, Pageable pageable);
}
