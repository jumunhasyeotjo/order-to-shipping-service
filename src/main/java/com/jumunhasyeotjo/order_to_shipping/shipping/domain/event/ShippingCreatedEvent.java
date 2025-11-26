package com.jumunhasyeotjo.order_to_shipping.shipping.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;

import lombok.Getter;

@Getter
public class ShippingCreatedEvent extends ShippingDomainEvent {
    private final UUID shippingId;
    private final UUID originHubId;
    private final String receiverName;
    private final String receiverPhoneNumber;
    private final LocalDateTime createdAt;
    private final String productInfo;
    private final String orderRequest;
    private final Long driverId;
    private final List<ShippingHistory> shippingHistories;

    public ShippingCreatedEvent(UUID shippingId, UUID originHubId, String receiverName, String receiverPhoneNumber,
        LocalDateTime createdAt, String productInfo, String orderRequest, Long driverId, List<ShippingHistory> shippingHistories) {
        this.shippingId = shippingId;
        this.originHubId = originHubId;
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
        this.createdAt = createdAt;
        this.productInfo = productInfo;
        this.orderRequest = orderRequest;
        this.driverId = driverId;
        this.shippingHistories = shippingHistories;
    }

}

