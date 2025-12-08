package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
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
    private final LocalDateTime orderCreatedTime;
    private final String productInfo;
    private final String requestMessage;
    private final List<UUID> supplierCompanyId;
    private final UUID receiverCompanyId;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(UUID orderId,
                             LocalDateTime orderCreatedTime,
                             String productInfo,
                             String requestMessage,
                             List<UUID> supplierCompanyId,
                             UUID receiverCompanyId) {
        this.orderId = orderId;
        this.orderCreatedTime = orderCreatedTime;
        this.productInfo = productInfo;
        this.requestMessage = requestMessage;
        this.supplierCompanyId = supplierCompanyId;
        this.receiverCompanyId = receiverCompanyId;
        this.occurredAt = LocalDateTime.now();
    }

    public static OrderCreatedEvent of(Order order, List<OrderCompany> orderCompanies) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getCreatedAt(),
                getProductInfo(orderCompanies.stream()
                        .flatMap(company -> company.getOrderProducts().stream())
                        .toList()),
                order.getRequestMessage(),
                orderCompanies.stream()
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