package com.jumunhasyeotjo.order_to_shipping.coupon.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.coupon.domain.vo.IssueCouponStatus;
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
@Table(
    name = "p_issue_coupon",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_issue_coupon_coupon_user",
            columnNames = {"coupon_id", "user_id"}
        )
    }
)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IssueCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private IssueCouponStatus status;

    private UUID orderId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static IssueCoupon issue(Coupon coupon, Long userId) {
        IssueCoupon issueCoupon = new IssueCoupon();
        issueCoupon.coupon = coupon;
        issueCoupon.userId = userId;
        issueCoupon.status = IssueCouponStatus.ISSUED;
        return issueCoupon;
    }

    public void useCoupon(UUID orderId) {
        if (status == IssueCouponStatus.ISSUED) {
            status = IssueCouponStatus.USED;
            this.orderId = orderId;
        }
        else throw new IllegalArgumentException("사용이 불가능한 쿠폰입니다.");
    }

    public void cancelCoupon(UUID orderId) {
        if (!orderId.equals(this.orderId)) {
            throw new IllegalArgumentException("쿠폰을 사용했던 주문이 아닙니다.");
        }
        if (status == IssueCouponStatus.USED) {
            if (coupon.getValidateEndDate().isBefore(LocalDate.now())) {
                status = IssueCouponStatus.EXPIRED;
            } else {
                status = IssueCouponStatus.ISSUED;
            }
        }
        else throw new IllegalArgumentException("취소가 불가능한 쿠폰입니다.");
    }

    public void expireCoupon() {
        if (status == IssueCouponStatus.ISSUED) status = IssueCouponStatus.EXPIRED;
        else throw new IllegalArgumentException("만료될 수 없는 쿠폰입니다.");
    }
}
