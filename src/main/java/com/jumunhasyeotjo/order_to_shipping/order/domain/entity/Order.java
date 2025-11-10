package com.jumunhasyeotjo.order_to_shipping.order.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_order")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private Long companyManagerId;

    @Column(columnDefinition = "TEXT")
    private String requestMessage;

    @Builder
    public Order(List<OrderProduct> orderProducts, Long companyManagerId, UUID companyId, String requestMessage, int totalPrice) {
        this.orderProducts = orderProducts;
        this.companyManagerId = companyManagerId;
        this.companyId = companyId;
        this.requestMessage = requestMessage;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
    }


    public static Order create(List<OrderProduct> orderProducts, Long companyManagerId, UUID companyId, String requestMessage, int totalPrice) {
        validateOrderProducts(orderProducts, totalPrice);

        return Order.builder()
                .orderProducts(orderProducts)
                .companyManagerId(companyManagerId)
                .companyId(companyId)
                .requestMessage(requestMessage)
                .totalPrice(totalPrice)
                .build();
    }

    public void update(List<OrderProduct> orderProducts, Long companyManagerId, String requestMessage, int totalPrice) {
        validateUpdate(companyManagerId);
        validateOrderProducts(orderProducts, totalPrice);

        this.orderProducts.clear();
        this.orderProducts.addAll(orderProducts);
        this.requestMessage = requestMessage;
        this.totalPrice = totalPrice;
    }

    public void cancel(UserRole role, Long companyManagerId) {
        validateCancel(role, companyManagerId);
        this.status = OrderStatus.CANCELLED;
    }

    public void updateStatus(OrderStatus status) {
        validateUpdateStatus();
        this.status = status;
    }


    private static void validateOrderProducts(List<OrderProduct> orderProducts, int totalPrice) {
        // 주문 상품 수량 검증
        if (orderProducts.isEmpty())
            throw new BusinessException(ErrorCode.EMPTY_ORDER_PRODUCTS);

        // total 가격 검증
        if (totalPrice != (orderProducts.stream().mapToInt(p -> p.getPrice() * p.getQuantity()).sum()))
            throw new BusinessException(ErrorCode.TOTAL_PRICE_MISMATCH);
    }

    private void validateUpdate(Long companyManagerId) {
        // 업체 담당자 검증
        if (!this.companyManagerId.equals(companyManagerId))
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_UPDATE);

        // 상태 검증
        if (!this.status.canUpdateOrder())
            throw new BusinessException(ErrorCode.INVALID_STATE_FOR_UPDATE);
    }

    private void validateCancel(UserRole role, Long companyManagerId) {
        // 업체 담당자
        if (role.equals(UserRole.COMPANY_MANAGER)) {
            if (!this.companyManagerId.equals(companyManagerId)) throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_CANCEL);
        }

        if (!this.status.canCancel()) throw new BusinessException(ErrorCode.INVALID_STATE_FOR_CANCEL);
    }

    private void validateUpdateStatus() {
        if (!this.status.canUpdateStatue()) throw new BusinessException(ErrorCode.ORDER_STATUS_UNCHANGEABLE);
    }
}
