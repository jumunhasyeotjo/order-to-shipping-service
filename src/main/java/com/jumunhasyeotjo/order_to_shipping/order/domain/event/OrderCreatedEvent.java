package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.UserResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class OrderCreatedEvent implements DomainEvent {

    private final UUID orderId;
    private final String slackId;
    private final LocalDateTime orderCreatedTime;
    private final String productInfo;
    private final String requestMessage;
    private final String receiverName;
    private final List<UUID> supplierCompanyId;
    private final UUID receiverCompanyId;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(UUID orderId,
                             String slackId,
                             LocalDateTime orderCreatedTime,
                             String productInfo,
                             String requestMessage,
                             String receiverName,
                             List<UUID> supplierCompanyId,
                             UUID receiverCompanyId) {
        this.orderId = orderId;
        this.slackId = slackId;
        this.orderCreatedTime = orderCreatedTime;
        this.productInfo = productInfo;
        this.requestMessage = requestMessage;
        this.receiverName = receiverName;
        this.supplierCompanyId = supplierCompanyId;
        this.receiverCompanyId = receiverCompanyId;
        this.occurredAt = LocalDateTime.now();
    }


    public static OrderCreatedEvent of(Order order, UserResult userInfo) {
        return new OrderCreatedEvent(
                order.getId(),
                userInfo.slackId(),
                order.getCreatedAt(),
                getProductInfo(order.getOrderCompanies().stream()
                        .flatMap(company -> company.getOrderProducts().stream())
                        .toList()),
                order.getRequestMessage(),
                userInfo.username(),
                order.getOrderCompanies().stream()
                        .map(OrderCompany::getSupplierCompanyId)
                        .toList(),
                order.getReceiverCompanyId()
        );
    }

    private static String getProductInfo(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(p -> String.format("%s %d박스", p.getName(), p.getQuantity()))
                .collect(Collectors.joining(", "));
    }
}
