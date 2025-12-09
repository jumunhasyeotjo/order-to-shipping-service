package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CancelOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.SearchOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.VendorOrder;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCancelledEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderRolledBackEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsNameRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order saveOrder(CreateOrderCommand command, List<VendorOrder> vendorOrders, int totalPrice) {
        return orderRepository.save(Order.create(
                vendorOrders,
                command.userId(),
                command.organizationId(),
                command.requestMessage(),
                totalPrice,
                command.idempotencyKey()));
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, OrderStatus status, List<VendorOrder> vendorOrders, RollbackStatus rollbackStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(status);

        if (status.equals(OrderStatus.ORDERED)) {
            eventPublisher.publishEvent(OrderCreatedEvent.of(order, vendorOrders));
        } else if (status.equals(OrderStatus.FAILED)) {
            eventPublisher.publishEvent(OrderRolledBackEvent.of(order.getId(), rollbackStatus));
        }
        return order;
    }

    @Transactional
    public Order cancelOrder(UserRole role, CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        order.cancel(role, command.userId());

        eventPublisher.publishEvent(OrderCancelledEvent.of(order.getId(), command.reason()));
        return order;
    }

    @Transactional(readOnly = true)
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return orderRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Order findByIdWithAll(UUID orderId) {
        Order order = orderRepository.findByIdWithAll(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.getVendorOrders().forEach(vo -> vo.getOrderProducts().size());
        return order;
    }

    @Transactional(readOnly = true)
    public Page<OrderResult> searchOrder(SearchOrderCommand command) {
        Page<Order> orders = orderRepository.findAllByCompanyId(command.companyId(), command.pageable());
        return orders.map(OrderResult::of);
    }

    @Transactional(readOnly = true)
    public List<VendorOrderItemsRes> getCompanyOrderItems(UUID vendorOrderId) {
        return orderRepository.findAllByVendorOrder(vendorOrderId);
    }

    @Transactional(readOnly = true)
    public List<VendorOrderItemsNameRes> getCompanyOrderItemsName(UUID vendorOrderId) {
        return orderRepository.findAllByVendorOrderName(vendorOrderId);
    }

    // 배치 처리시 다른 트랜잭션에 영향을 주지않도록 REQUIRES_NEW 설정으로 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackSingleOrder(Order order) {
        order.updateStatus(OrderStatus.FAILED);

        OrderRolledBackEvent event = OrderRolledBackEvent.of(order.getId(), RollbackStatus.FULL_ROLLBACK);
        eventPublisher.publishEvent(event);
    }
}
