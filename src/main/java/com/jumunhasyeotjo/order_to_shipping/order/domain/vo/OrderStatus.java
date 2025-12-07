package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("주문 대기"),
    ORDERED("주문 완료"),
    FAILED("주문 실패"),
    CANCELLED("주문 취소"),
    SHIPPED("배송중"),
    DONE("배송 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public boolean canCancel() {
        return this.equals(ORDERED);
    }

    public boolean canUpdateStatue() {
        return !(this.equals(DONE) || this.equals(CANCELLED));

    }
}
