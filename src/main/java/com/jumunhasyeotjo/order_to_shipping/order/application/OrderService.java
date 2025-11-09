package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        UUID companyId = getUserCompanyId(command.userId());
        validateCompany(companyId);
        List<OrderProduct> orderProducts = findAllOrderProduct(command.orderProducts());
        decreaseStock(command.orderProducts());

        Order savedOrder = orderRepository.save(Order.create(orderProducts, command.userId(), companyId, command.requestMessage(), command.totalPrice()));
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

        validateHubManager(command.role(), command.userId(), order.getCompanyId());
        order.updateStatus(command.status());

        return order;
    }

    // 허브 담당자 검증
    private void validateHubManager(String role, Long userId, UUID companyId) {
        if (role.equals("HUB_MANAGER")) {
            UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
            if (hubId == null) throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
            if (!companyClient.existCompanyRegionalHub(companyId, hubId)) throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
        }
    }


    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateHubManager(command.role(), command.userId(), order.getCompanyId());

        order.cancel(command.role(), null);

        return order;
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(GetOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateGetOrder(command.role(), command.userId(), order);
        return OrderResult.of(order);
    }

    private void validateGetOrder(String role, Long userId, Order order) {
        switch (role) {
            case "HUB_MANAGER":
                UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
                if (!companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;
            case "COMPANY_MANAGER":
                if (!order.getCompanyManagerId().equals(userId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResult> searchOrder(SearchOrderCommand command) {
        validSearchOrder(command.role(), command.userId(), command.companyId());
        List<Order> orderList = orderRepository.findAllByCompanyId(command.companyId(), command.pageable());

        return orderList.stream().map(OrderResult::of).toList();
    }

    private void validSearchOrder(String role, Long userId, UUID companyId) {
        switch (role) {
            case "COMPANY_MANAGER":
                if (!companyId.equals(userClient.getCompanyId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN))))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;

            case "HUB_MANAGER":
                UUID hubId = userClient.getHubId(userId).orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
                if (!companyClient.existCompanyRegionalHub(companyId, hubId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
        }
    }
}
