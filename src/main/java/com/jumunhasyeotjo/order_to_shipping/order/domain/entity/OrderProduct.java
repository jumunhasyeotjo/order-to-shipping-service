package com.jumunhasyeotjo.order_to_shipping.order.domain.entity;

import com.jumunhasyeotjo.order_to_shipping.common.entity.BaseEntity;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_order_product")
public class OrderProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_product_id")
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String name;

    @Builder
    public OrderProduct(UUID productId, int price, int quantity, String name) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.name = name;
    }

    public static OrderProduct create(UUID productId, int price, int quantity, String name) {
        validateOrderProduct(price, quantity);
        return OrderProduct.builder()
                .productId(productId)
                .price(price)
                .quantity(quantity)
                .name(name)
                .build();
    }

    private static void validateOrderProduct(int price, int quantity) {
        if (price <= 0) throw new BusinessException(ErrorCode.INVALID_PRODUCT_PRICE);
        if (quantity <= 0) throw new BusinessException(ErrorCode.INVALID_PRODUCT_QUANTITY);
    }


}
