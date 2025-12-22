package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository;

import com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity.IssueCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaIssueCouponRepository extends JpaRepository<IssueCoupon, UUID> {
    boolean existsByCoupon_CouponIdAndUserId(UUID couponId, Long userId);
    Optional<IssueCoupon> findByOrderId(UUID orderId);
    List<IssueCoupon> findByUserId(Long userId);
}
