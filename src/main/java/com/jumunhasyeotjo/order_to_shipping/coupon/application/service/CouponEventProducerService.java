package com.jumunhasyeotjo.order_to_shipping.coupon.application.service;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.event.CouponIssueEvent;

public interface CouponEventProducerService {
    void sendIssueEvent(CouponIssueEvent event);
}
