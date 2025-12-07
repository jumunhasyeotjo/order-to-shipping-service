package com.jumunhasyeotjo.order_to_shipping.order.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
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
    @Column(name = "order_id")
    private UUID id;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private UUID receiverCompanyId;

    @Column(nullable = false)
    private Long companyManagerId;

    @Column(columnDefinition = "TEXT")
    private String requestMessage;

    @Column(nullable = false)
    private String idempotencyKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderCompany> orderCompanies = new ArrayList<>();

    @Builder
    public Order(List<OrderCompany> orderCompanies, Long companyManagerId, UUID receiverCompanyId, String requestMessage, int totalPrice, String idempotencyKey) {
        this.orderCompanies = orderCompanies;
        this.companyManagerId = companyManagerId;
        this.receiverCompanyId = receiverCompanyId;
        this.requestMessage = requestMessage;
        this.totalPrice = totalPrice;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.PENDING;
    }


    public static Order create(List<OrderCompany> orderCompanies, Long companyManagerId, UUID receiverCompanyId, String requestMessage, int totalPrice, String idempotencyKey) {
        validateCreate(orderCompanies, companyManagerId, receiverCompanyId, totalPrice);

        Order order = Order.builder()
                .orderCompanies(orderCompanies)
                .companyManagerId(companyManagerId)
                .receiverCompanyId(receiverCompanyId)
                .requestMessage(requestMessage)
                .totalPrice(totalPrice)
                .idempotencyKey(idempotencyKey)
                .build();

        for (OrderCompany orderCompany : orderCompanies) {
            orderCompany.setOrder(order);
        }

        return order;
    }

    public void cancel(UserRole role, Long companyManagerId) {
        validateCancel(role, companyManagerId);
        this.status = OrderStatus.CANCELLED;
    }

    public void updateStatus(OrderStatus status) {
        validateUpdateStatus();
        this.status = status;
    }


    private static void validateCreate(List<OrderCompany> orderCompanies, Long companyManagerId, UUID receiverCompanyId, int totalPrice) {
        if (orderCompanies.isEmpty() || totalPrice < 0 || companyManagerId == null || receiverCompanyId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
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
