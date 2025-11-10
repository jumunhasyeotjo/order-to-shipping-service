package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("결제 대기"),
    PAYED("결제 완료"),
    SHIPPED("배송중"),
    DONE("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public boolean canUpdateOrder() {
        return this.equals(PENDING);
    }

    public boolean canCancel() {
        return this.equals(PENDING) || this.equals(PAYED);
    }

    public boolean canUpdateStatue() {
        return !(this.equals(DONE) || this.equals(CANCELLED));

    }
}
