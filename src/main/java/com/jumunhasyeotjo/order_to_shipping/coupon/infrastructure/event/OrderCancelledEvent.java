package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID orderId,
    String cancelReason,
    LocalDateTime occurredAt
) {
}
