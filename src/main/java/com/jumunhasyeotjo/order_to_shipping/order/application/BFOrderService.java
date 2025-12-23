package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCouponClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.VendorOrder;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BFOrderService {
    private final OrderService orderService;
    private final OrderProductClient orderProductClient;
    private final OrderCompanyClient orderCompanyClient;
    private final OrderCouponClient orderCouponClient;
    private final BFOrderOutboxService BFOrderOutboxService;
    /**
     * [Tx-1] Request SnapShot 저장
     * - 별도 트랜잭션으로 분리
     * - Order 엔티티 PENDING 상태로 저장
     * - 즉시 커밋
     */
    @Transactional
    public CreateOrderSnapshotDto createOrderSnapshot(CreateOrderCommand command) {
        log.info("[Tx-1 Start] Request SnapShot 저장 시작");

        // 1. 검증
        validateCompany(command.organizationId());
        validateDuplicateOrder(command.idempotencyKey());

        // 2. 상품 조회 및 검증
        List<ProductResult> productResults = findAllOrderProduct(command.orderProducts());

        Integer discountPrice = findCoupon(command.couponId());

        // 3. 데이터 매핑
        Map<UUID, Integer> productQuantityMap = getProductQuantityMap(command);
        List<VendorOrder> vendorOrders = mapVendorOrders(productResults, productQuantityMap);
        int totalPrice = mapTotalPrice(productResults, productQuantityMap);

        // 4. Order 저장 (PENDING 상태)
        Order pendingOrder = orderService.saveOrder(command, vendorOrders, totalPrice);

        // 5. outbox 초기화
        BFOrderOutboxService.
                createOutbox(pendingOrder.getId(), pendingOrder.getId().toString(), "OrderCreated", null);


        log.info("[Tx-1 End] Order 저장 완료 - orderId: {}", pendingOrder.getId());

        // Tx-1 커밋 (메서드 종료 시)
        return new CreateOrderSnapshotDto(pendingOrder,discountPrice);
    }

    private Integer findCoupon(UUID couponId) {
//        Integer discountPrice = orderCouponClient.useCoupon(couponId, orderId);
//        if (discountPrice == null || discountPrice == 0) {
//            throw new BusinessException(ErrorCode.INVALID_INPUT);
//        }
//        return discountPrice;
        return 1;
    }

    // ========== 검증 메서드 ==========

    private void validateDuplicateOrder(String idempotencyKey) {
        if (orderService.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("[검증 실패] 중복 주문 요청 - idempotencyKey: {}", idempotencyKey);
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
    }

    private void validateCompany(UUID companyId) {
        if (!orderCompanyClient.existCompany(companyId).data()) {
            log.error("[검증 실패] 존재하지 않는 업체 - companyId: {}", companyId);
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private List<ProductResult> findAllOrderProduct(List<OrderProductReq> orderProducts) {
        List<ProductResult> allProducts = orderProductClient
                .findAllProducts(orderProducts.stream()
                        .map(OrderProductReq::productId)
                        .toList())
                .data();

        if (allProducts.size() != orderProducts.size()) {
            log.error("[검증 실패] 상품 정보 불일치 - 요청: {}, 조회: {}",
                    orderProducts.size(), allProducts.size());
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return allProducts;
    }

    // ========== 데이터 매핑 메서드 ==========

    private Map<UUID, Integer> getProductQuantityMap(CreateOrderCommand command) {
        return command.orderProducts().stream()
                .collect(Collectors.toMap(
                        OrderProductReq::productId,
                        OrderProductReq::quantity
                ));
    }

    private List<VendorOrder> mapVendorOrders(
            List<ProductResult> productResults,
            Map<UUID, Integer> quantityMap
    ) {
        Map<UUID, List<ProductResult>> productByCompany = productResults.stream()
                .collect(Collectors.groupingBy(ProductResult::companyId));

        List<VendorOrder> vendorOrders = new ArrayList<>();

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

            vendorOrders.add(VendorOrder.create(supplierCompanyId, orderProducts));
        }

        return vendorOrders;
    }

    private int mapTotalPrice(
            List<ProductResult> productResults,
            Map<UUID, Integer> quantityMap
    ) {
        return productResults.stream()
                .mapToInt(p -> p.price() * quantityMap.get(p.productId()))
                .sum();
    }

    /**
     * [Final Stage] 결제 승인 + Outbox/SnapShot 상태 갱신
     *
     * - 결제 승인 (출금) 호출
     * - Outbox 상태를 'complete'로 갱신
     * - Order 상태를 'ORDERED'로 갱신
     * - 두 상태 갱신은 하나의 트랜잭션으로 묶음
     */
    @Transactional
    public Order executeStatusUpdate(UUID orderId) {
        log.info("[Final Stage Start] orderId: {}", orderId);

        try {

            // → 하나의 트랜잭션으로 묶음
            BFOrderOutboxService.markAsReady(orderId);
            Order completedOrder = orderService.updateOrderStatus(
                    orderId,
                    OrderStatus.ORDERED,
                    null,
                    null
            );

            log.info("[Final Stage End] Outbox/SnapShot 상태 갱신 완료 - orderId: {}", orderId);

            return completedOrder;

        } catch (Exception e) {
            log.error("[Final Stage 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FINAL_STAGE_FAILED);
        }
    }
}
