package com.jumunhasyeotjo.order_to_shipping.order.domain.event;

import com.jumunhasyeotjo.order_to_shipping.common.event.DomainEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.VendorOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class OrderCreatedEvent implements DomainEvent {

    private UUID orderId;
    private LocalDateTime orderCreatedTime;
    private String requestMessage;
    private UUID receiverCompanyId;
    private LocalDateTime occurredAt;
    private List<VendingOrder> vendingOrders;

    public OrderCreatedEvent(UUID orderId,
                             LocalDateTime orderCreatedTime,
                             String requestMessage,
                             UUID receiverCompanyId,
                             List<VendingOrder> vendingOrders) {
        this.orderId = orderId;
        this.orderCreatedTime = orderCreatedTime;
        this.requestMessage = requestMessage;
        this.receiverCompanyId = receiverCompanyId;
        this.vendingOrders = vendingOrders;
        this.occurredAt = LocalDateTime.now();
    }

    public static OrderCreatedEvent of(Order order, List<VendorOrder> vendorOrders) {
        // 1. 엔티티(VendorOrder) 리스트 이벤트용 DTO(VendingOrder) 리스트로 변환
        List<VendingOrder> vendingOrderDtos = vendorOrders.stream()
                .map(vo -> new VendingOrder(
                        vo.getId(),
                        vo.getSupplierCompanyId(),
                        getProductInfo(vo.getOrderProducts()) // 각 공급사별 주문 상품 정보 생성
                ))
                .toList();

        // 2. 이벤트 객체 생성
        return new OrderCreatedEvent(
                order.getId(),
                order.getCreatedAt(),
                order.getRequestMessage(),
                order.getReceiverCompanyId(),
                vendingOrderDtos
        );
    }

    // 상품 정보를 String으로 변환하는 유틸리티 메서드
    private static String getProductInfo(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(p -> String.format("%s %d박스", p.getName(), p.getQuantity()))
                .collect(Collectors.joining(", "));
    }

    // 이벤트 내부에서 사용할 VendingOrder 구조
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VendingOrder {
        private UUID vendingOrderId;
        private UUID supplierCompanyId;
        private String productInfo;
    }
}