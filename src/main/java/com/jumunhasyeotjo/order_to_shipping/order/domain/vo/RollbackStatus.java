package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

import lombok.Getter;

@Getter
public enum RollbackStatus {
    NONE("초기 상태"),
    USE_COUPON("쿠폰 사용"),
    DECREASE_STOCK("재고 차감"),
    PAYED_ORDER("결제 처리"),
    FULL_ROLLBACK("서버 크래시");

    private final String description;

    RollbackStatus(String description) {
        this.description = description;
    }
}
