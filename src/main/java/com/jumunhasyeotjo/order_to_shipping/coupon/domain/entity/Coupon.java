package com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_coupon")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID couponId;

    @Column(nullable = false)
    private String couponName;

    @Column(nullable = false)
    private Integer discountAmount;

    private Integer maxQuantity;

    @Column(nullable = false)
    private Integer issuedQuantity;

    @Column(nullable = false)
    private LocalDate validateStartDate;

    @Column(nullable = false)
    private LocalDate validateEndDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Coupon createCoupon(
        String couponName,
        Integer discountAmount,
        Integer maxQuantity,
        LocalDate validateStartDate,
        LocalDate validateEndDate
    ) {
        if (validateStartDate.isAfter(validateEndDate)) {
            throw new IllegalArgumentException("쿠폰 유효일이 유효하지 않습니다.");
        }
        Coupon coupon = new Coupon();
        coupon.couponName = couponName;
        coupon.discountAmount = discountAmount;
        coupon.maxQuantity = maxQuantity;
        coupon.issuedQuantity = 0;
        coupon.validateStartDate = validateStartDate;
        coupon.validateEndDate = validateEndDate;
        return coupon;
    }

    public void issueCoupon() {
        if (this.maxQuantity < this.issuedQuantity + 1) {
            throw new IllegalArgumentException("쿠폰 최대 발급 개수를 초과했습니다.");
        } else {
            issuedQuantity++;
        }
    }
}
