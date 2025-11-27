package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    private final OrderRepository orderRepository;

    private final StockClient stockClient;
    private final ProductClient productClient;
    private final OrderCompanyClient orderCompanyClient;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        validateCompany(command.organizationId());

        List<ProductResult> productResult = findAllOrderProduct(command.orderProducts());

        decreaseStock(command.orderProducts());

        Map<UUID, Integer> productQuantityMap = command.orderProducts().stream()
                .collect(Collectors.toMap(OrderProductReq::productId, OrderProductReq::quantity));

        List<OrderCompany> orderCompanies = mapOrderCompany(productResult, productQuantityMap);

        int totalPrice = mapTotalPrice(productResult, productQuantityMap);

        Order savedOrder = orderRepository.save(Order.create(
                orderCompanies,
                command.userId(),
                command.organizationId(),
                command.requestMessage(),
                totalPrice
        ));

        eventPublisher.publishEvent(OrderCreatedEvent.of(savedOrder));

        return savedOrder;
    }

    private List<OrderCompany> mapOrderCompany(List<ProductResult> productResult, Map<UUID, Integer> quantityMap) {
        // 공급 업체 ID 기준 그룹화
        Map<UUID, List<ProductResult>> productByCompany = productResult.stream()
                .collect(Collectors.groupingBy(ProductResult::companyId));

        List<OrderCompany> orderCompanies = new ArrayList<>();

        // 그룹핑된 데이터기반 OrderCompany 및 OrderProduct 생성
        for (Map.Entry<UUID, List<ProductResult>> entry : productByCompany.entrySet()) {
            UUID supplierCompanyId = entry.getKey();
            List<ProductResult> products = entry.getValue();

            List<OrderProduct> orderProducts = products.stream()
                    .map(p -> OrderProduct.create(
                            p.productId(),
                            p.price(),
                            quantityMap.get(p.productId()),
                            p.name()
                    ))
                    .toList();

            orderCompanies.add(OrderCompany.create(supplierCompanyId, orderProducts));
        }

        return orderCompanies;
    }

    // 총 가격 계산
    private int mapTotalPrice(List<ProductResult> productResult, Map<UUID, Integer> quantityMap) {
        return productResult.stream().mapToInt(p -> p.price() * quantityMap.get(p.productId())).sum();
    }

    // 상품 정보 조회 및 검증
    private List<ProductResult> findAllOrderProduct(List<OrderProductReq> orderProducts) {
        List<ProductResult> allProducts = productClient.findAllProducts(orderProducts.stream().map(OrderProductReq::productId).toList());
        if (!(allProducts.size() == orderProducts.size())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return allProducts;
    }

    // 재고 검증 및 차감
    private void decreaseStock(List<OrderProductReq> orderProducts) {
        if (!stockClient.decreaseStock(orderProducts)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STOCK);
        }
    }

    // 존재하는 업체 검증
    private void validateCompany(UUID companyId) {
        if (!orderCompanyClient.existCompany(companyId))
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
    }

    @Transactional
    public Order updateOrderStatus(OrderUpdateStatusCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateHubManager(command.organizationId(), UserRole.convertToUserRole(command.role()), order.getReceiverCompanyId());

        order.updateStatus(command.status());

        return order;
    }

    // 허브 담당자 검증
    private void validateHubManager(UUID organizationId, UserRole role, UUID companyId) {
        if (role.equals(UserRole.HUB_MANAGER)) {
            if (!orderCompanyClient.existCompanyRegionalHub(companyId, organizationId))
                throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
        }
    }


    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        UserRole role = UserRole.convertToUserRole(command.role());

        validateHubManager(command.organizationId(), role, order.getReceiverCompanyId());

        restoreStock(order);

        order.cancel(role, command.userId());

        return order;
    }

    // 재고 복구
    private void restoreStock(Order order) {
        List<OrderProduct> orderProducts = order.getOrderCompanies().stream()
                .flatMap(company -> company.getOrderProducts().stream())
                .toList();

        stockClient.restoreStocks(orderProducts);
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(GetOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateGetOrder(command.organizationId(), UserRole.convertToUserRole(command.role()), command.userId(), order);

        return OrderResult.of(order);
    }

    private void validateGetOrder(UUID organizationId, UserRole role, Long userId, Order order) {
        switch (role) {
            case HUB_MANAGER:
                if (!orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), organizationId))
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
        validSearchOrder(command.organizationId(), UserRole.convertToUserRole(command.role()), command.companyId());

        Page<Order> orderList = orderRepository.findAllByCompanyId(command.companyId(), command.pageable());

        return orderList.map(OrderResult::of);
    }

    private void validSearchOrder(UUID organizationId, UserRole role, UUID companyId) {
        switch (role) {
            case COMPANY_MANAGER:
                if (!companyId.equals(organizationId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
                break;

            case HUB_MANAGER:
                if (!orderCompanyClient.existCompanyRegionalHub(companyId, organizationId))
                    throw new BusinessException(ErrorCode.FORBIDDEN_GET_ORDER);
        }
    }
}
