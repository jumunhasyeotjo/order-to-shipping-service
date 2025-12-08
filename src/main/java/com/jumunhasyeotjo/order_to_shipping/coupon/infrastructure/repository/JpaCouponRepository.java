package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository;


import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaCouponRepository extends JpaRepository<Coupon, UUID> {
}
