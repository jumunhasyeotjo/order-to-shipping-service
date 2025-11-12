package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

public enum OrderStatus {
    PENDING, PAYED, SHIPPED, DONE, CANCELLED;

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
