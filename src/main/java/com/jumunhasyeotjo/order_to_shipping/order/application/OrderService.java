package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.*;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final StockClient stockClient;
    private final ProductClient productClient;
    private final CompanyClient companyClient;
    private final UserClient userClient;
    private final DeliveryClient deliveryClient;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        UUID companyId = getUserCompanyId(command.userId());
        validateCompany(companyId);
        List<OrderProduct> orderProducts = findAllOrderProduct(command.orderProducts());
        decreaseStock(command.orderProducts());

        Order savedOrder = orderRepository.save(Order.create(orderProducts, command.userId(), companyId, command.requestMessage(), command.totalPrice()));

        // Todo : Delivery Client 배송 생성시 필요한 정보 추가
        deliveryClient.createDelivery();

        return savedOrder;
    }

    // 상품 정보 조회 및 검증
    private List<OrderProduct> findAllOrderProduct(List<OrderProductReq> orderProducts) {
        List<OrderProduct> findProducts = productClient.findAllProducts(orderProducts).stream()
                .map(p -> OrderProduct.create(p.productId(), p.price(), p.quantity(), p.name()))
                .toList();
        if (findProducts.size() != orderProducts.size()) throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        return findProducts;
    }

    // 재고 검증 및 차감
    private void decreaseStock(List<OrderProductReq> orderProducts) {
        if (!stockClient.decreaseStock(orderProducts)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STOCK);
        }
    }

    // 사용자 업체 ID 조회
    private UUID getUserCompanyId(Long userId) {
        UUID companyId = userClient.getCompanyId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        if (companyId == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return companyId;
    }

    // 존재하는 업체 검증
    private void validateCompany(UUID companyId) {
        if (!companyClient.existCompany(companyId))
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
    }

    @Transactional
    public Order updateOrder(UpdateOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderProduct> orderProducts = findAllOrderProduct(command.orderProducts());
        decreaseStock(command.orderProducts());

        order.update(orderProducts, command.userId(), command.requestMessage(), command.totalPrice());
        return order;
    }

    @Transactional
    public Order updateOrderStatus(OrderUpdateStatusCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateHubManager(UserRole.convertToUserRole(command.role()), command.userId(), order.getCompanyId());
        order.updateStatus(command.status());

        return order;
    }

    // 허브 담당자 검증
    private void validateHubManager(UserRole role, Long userId, UUID companyId) {
        if (role.equals(UserRole.HUB_MANAGER)) {
            UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
            if (hubId == null) throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
            if (!companyClient.existCompanyRegionalHub(companyId, hubId)) throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
        }
    }


    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        UserRole role = UserRole.convertToUserRole(command.role());
        validateHubManager(role, command.userId(), order.getCompanyId());

        restoreStock(order);
        order.cancel(role, command.userId());
        return order;
    }

    // 재고 복구
    private void restoreStock(Order order) {
        stockClient.restoreStocks(order.getOrderProducts());
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(GetOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateGetOrder(UserRole.convertToUserRole(command.role()), command.userId(), order);
        return OrderResult.of(order);
    }

    private void validateGetOrder(UserRole role, Long userId, Order order) {
        switch (role) {
            case HUB_MANAGER:
                UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
                if (!companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;
            case COMPANY_MANAGER:
                if (!order.getCompanyManagerId().equals(userId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResult> searchOrder(SearchOrderCommand command) {
        validSearchOrder(UserRole.convertToUserRole(command.role()), command.userId(), command.companyId());
        Page<Order> orderList = orderRepository.findAllByCompanyId(command.companyId(), command.pageable());

        return orderList.map(OrderResult::of);
    }

    private void validSearchOrder(UserRole role, Long userId, UUID companyId) {
        switch (role) {
            case COMPANY_MANAGER:
                if (!companyId.equals(userClient.getCompanyId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN))))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;

            case HUB_MANAGER:
                UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
                if (!companyClient.existCompanyRegionalHub(companyId, hubId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
        }
    }
}
