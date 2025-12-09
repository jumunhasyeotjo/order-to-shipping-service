package com.jumunhasyeotjo.order_to_shipping.order.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
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
@Table(name = "p_vendor_order")
public class VendorOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vendor_order_id")
    private UUID id;

    @Column(nullable = false)
    private UUID supplierCompanyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(mappedBy = "vendorOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
    
    @Builder
    public VendorOrder(UUID supplierCompanyId, List<OrderProduct> orderProducts) {
        this.supplierCompanyId = supplierCompanyId;
        this.orderProducts = orderProducts;
    }

    public static VendorOrder create(UUID supplierCompanyId, List<OrderProduct> orderProducts) {
        validateOrderCompany(supplierCompanyId, orderProducts);
        VendorOrder vendorOrder = VendorOrder.builder()
                .supplierCompanyId(supplierCompanyId)
                .orderProducts(orderProducts)
                .build();

        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setVendorOrder(vendorOrder);
        }
        return vendorOrder;
    }

    private static void validateOrderCompany(UUID supplierCompanyId, List<OrderProduct> orderProducts) {
        if (orderProducts.isEmpty() || supplierCompanyId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}


