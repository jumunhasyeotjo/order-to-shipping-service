package com.jumunhasyeotjo.order_to_shipping.stock.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stock_id")
    private UUID id;

    private UUID productId;

    private int quantity;

    @Builder
    public Stock(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public void decreaseStock(int quantity) {
        if (this.quantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        this.quantity -= quantity;
    }
}
