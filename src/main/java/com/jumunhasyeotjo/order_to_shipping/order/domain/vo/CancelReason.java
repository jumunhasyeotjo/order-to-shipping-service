package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

import lombok.Getter;

@Getter
public enum CancelReason {
    // 고객 사유
    SIMPLE_CHANGE_OF_MIND("단순 변심"),
    MISTAKE_ORDER("주문 실수 (수량/옵션 등)"),

    // 판매자 사유
    OUT_OF_STOCK("재고 부족"),
    DELAYED_SHIPMENT("배송 지연"),

    // 기타 사유
    ETC("기타 사유");

    private final String description;

    CancelReason(String description) {
        this.description = description;
    }
}
