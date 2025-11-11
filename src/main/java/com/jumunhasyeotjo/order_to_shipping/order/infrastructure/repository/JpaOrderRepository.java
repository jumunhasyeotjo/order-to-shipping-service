package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<Order, UUID>{

    @Query(value = "select distinct o from Order o " +
            "join fetch o.orderProducts op " +
            "where o.companyId = :companyId",
            countQuery = "select count(o) from Order o where o.companyId = :companyId")
    Page<Order> findAllByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);
}
