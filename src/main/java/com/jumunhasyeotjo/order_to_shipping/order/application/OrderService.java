package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.*;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CompanyOrderItemsRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderPersistenceService orderPersistenceService;

    private final OrderStockClient orderStockClient;
    private final OrderProductClient orderProductClient;
    private final OrderCompanyClient orderCompanyClient;
    private final OrderCouponClient orderCouponClient;
    private final OrderPaymentClient orderPaymentClient;

    // 주문 등록
    public Order createOrder(CreateOrderCommand command) {
        validateCompany(command.organizationId());
        validateDuplicateOrder(command.idempotencyKey()); // 중복 주문 검증

        List<ProductResult> productResult = findAllOrderProduct(command.orderProducts()); // 상품 조회

        // 데이터 매핑
        Map<UUID, Integer> productQuantityMap = getProductQuantityMap(command);
        List<OrderCompany> orderCompanies = mapOrderCompany(productResult, productQuantityMap);
        int totalPrice = mapTotalPrice(productResult, productQuantityMap);

        Order pendingOrder = orderPersistenceService.saveOrder(command, orderCompanies, totalPrice); // DB 저장 (PENDING)
        UUID pendingOrderId = pendingOrder.getId(); // orderId를 멱등키로 활용

        RollbackStatus status = RollbackStatus.NONE;

        try {
            status = RollbackStatus.USE_COUPON;
            useCoupon(pendingOrderId);

            status = RollbackStatus.DECREASE_STOCK;
            decreaseStock(command.orderProducts(), pendingOrderId);

            status = RollbackStatus.PAYED_ORDER;
            payOrder(totalPrice, pendingOrderId);

            // 주문 확정
            return orderPersistenceService.updateOrderStatus(pendingOrderId, OrderStatus.ORDERED, orderCompanies, null);

        } catch (Exception e) {
            log.error("주문 저장 실패로 인한 보상 트랜잭션 실행: {}", e.getMessage());
            orderPersistenceService.updateOrderStatus(pendingOrderId, OrderStatus.FAILED, null, status);
            throw e; // 롤백을 위한 예외 발생
        }
    }

    private void validateDuplicateOrder(String idempotencyKey) {
        if (orderPersistenceService.existsByIdempotencyKey(idempotencyKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
    }

    // 데이터 매핑
    private Map<UUID, Integer> getProductQuantityMap(CreateOrderCommand command) {
        return command.orderProducts().stream()
                .collect(Collectors.toMap(OrderProductReq::productId, OrderProductReq::quantity));
    }

    // Entity 바인딩 (등록)
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

    // 총 가격 계산 (등록)
    private int mapTotalPrice(List<ProductResult> productResult, Map<UUID, Integer> quantityMap) {
        return productResult.stream().mapToInt(p -> p.price() * quantityMap.get(p.productId())).sum();
    }

    // 상품 정보 조회 및 검증 (등록)
    private List<ProductResult> findAllOrderProduct(List<OrderProductReq> orderProducts) {
        List<ProductResult> allProducts = orderProductClient.findAllProducts(orderProducts.stream().map(OrderProductReq::productId).toList());
        if (!(allProducts.size() == orderProducts.size())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return allProducts;
    }
    // 쿠폰 사용 (등록)
    private void useCoupon(UUID orderId) {
        if (!orderCouponClient.useCoupon(UUID.randomUUID(), orderId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
    // 재고 검증 및 차감 (등록)
    private void decreaseStock(List<OrderProductReq> orderProducts, UUID orderId) {
        if (!orderStockClient.decreaseStock(orderProducts, orderId)) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_STOCK);
        }
    }
    // 주문 결제 (등록)
    private void payOrder(int totalPrice, UUID orderId) {
        if (!orderPaymentClient.confirmOrder(totalPrice, orderId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    // 존재하는 업체 검증 (등록)
    private void validateCompany(UUID companyId) {
        if (!orderCompanyClient.existCompany(companyId))
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
    }

    // 주문 상태 변경
    public Order updateOrderStatus(OrderUpdateStatusCommand command) {
        Order order = orderPersistenceService.findById(command.orderId());

        validateHubManager(command.organizationId(), UserRole.convertToUserRole(command.role()), order.getReceiverCompanyId());

        return orderPersistenceService.updateOrderStatus(order.getId(), command.status(), null, null);
    }

    // 허브 담당자 검증 (상태 변경, 취소)
    private void validateHubManager(UUID organizationId, UserRole role, UUID companyId) {
        if (role.equals(UserRole.HUB_MANAGER)) {
            if (!orderCompanyClient.existCompanyRegionalHub(companyId, organizationId))
                throw new BusinessException(ErrorCode.FORBIDDEN_ORDER_HUB);
        }
    }

    // 주문 취소
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderPersistenceService.findById(command.orderId());
        UserRole role = UserRole.convertToUserRole(command.role());

        validateHubManager(command.organizationId(), role, order.getReceiverCompanyId());

        return orderPersistenceService.cancelOrder(role, command);
    }

    // 주문 단건 조회
    public OrderResult getOrder(GetOrderCommand command) {
        Order order = orderPersistenceService.findByIdWithAll(command.orderId());

        validateGetOrder(command.organizationId(), UserRole.convertToUserRole(command.role()), command.userId(), order);

        return OrderResult.of(order);
    }

    // 단건 조회 검증 (단건 조회)
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

    // 주문 다건 조회
    public Page<OrderResult> searchOrder(SearchOrderCommand command) {
        validSearchOrder(command.organizationId(), UserRole.convertToUserRole(command.role()), command.companyId());
        return orderPersistenceService.searchOrder(command);
    }

    // 다건 조회 검증 (다건 조회)
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

    // 주문 상품 공급 업체별 조회
    public List<CompanyOrderItemsRes> getCompanyOrderItems(UUID companyOrderId) {
        return orderPersistenceService.getCompanyOrderItems(companyOrderId);
    }
}
