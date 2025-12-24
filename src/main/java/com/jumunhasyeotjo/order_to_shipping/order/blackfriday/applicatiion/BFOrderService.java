package com.jumunhasyeotjo.order_to_shipping.order.blackfriday.applicatiion;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.CreateOrderSnapshotDto;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderCompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderPaymentClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.VendorOrder;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BFOrderService {
    private final OrderService orderService;
    private final OrderProductClient orderProductClient;
    private final OrderCompanyClient orderCompanyClient;
    private final OrderPaymentClient orderPaymentClient;
    private final RedisOutboxPublisher redisOutboxPublisher;
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
    public Order executeStatusUpdate(Order order) {
        UUID orderId = order.getId();
        log.info("[Final Stage Start] orderId: {}", orderId);

        try {

            // → 하나의 트랜잭션으로 묶음
            BFOrderOutboxService.markAsReady(orderId);
            Order completedOrder = orderService.updateOrderStatus(
                    orderId,
                    OrderStatus.ORDERED,
                    order.getVendorOrders(),
                    null
            );

            log.info("[Final Stage End] Outbox/SnapShot 상태 갱신 완료 - orderId: {}", orderId);

            return completedOrder;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("[Final Stage 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FINAL_STAGE_FAILED);
        }
    }

    public Order order(CreateOrderCommand command, Order pendingOrder, UUID orderId, CreateOrderSnapshotDto snapshotDto, RollbackStatus status) {
        try {

            // 2. [Lua Atomic] 재고/쿠폰 예약 + Redis Streams Outbox 발행
            String messageId = publishOrderCreatedEvent(pendingOrder, command);
            log.info("[Lua Atomic 완료] Outbox 발행 - messageId: {}, orderId: {}", messageId, orderId);

            //해당 지점에서 Crash 발생시 스케줄러를 통해 publishOrderCreatedEvent 보상

            // 3. [Final Stage] 결제 승인 + 상태 갱신
            int paymentPrice = pendingOrder.getTotalPrice() - snapshotDto.getDiscountPrice();
            Order completedOrder = executeFinalStage(pendingOrder, paymentPrice, command);
            log.info("[Final Stage 완료] 주문 완료 - orderId: {}, status: ORDERED", orderId);

            // 4. 응답 반환
            return completedOrder;

        } catch (Exception e) {
            log.error("[주문 생성 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            log.warn("[Compensate] 주문 실패 롤백 실행 - 단계: {}, 사유: {}", status, e.getMessage());
            orderService.updateOrderStatus(orderId, OrderStatus.FAILED, null, status);
            e.printStackTrace();
            throw e;
        }
    }

    public Order executeFinalStage(Order order, int totalPrice, CreateOrderCommand command) {
        UUID orderId = order.getId();
        log.info("[Final Stage Start] orderId: {}", orderId);

        try {
            // 1. 결제 승인 (출금) 호출
            boolean paymentSuccess = orderPaymentClient.confirmOrder(
                    totalPrice,
                    command.tossPaymentKey(),
                    command.tossOrderId(),
                    orderId
            );

            if (!paymentSuccess) {
                throw new BusinessException(ErrorCode.PAYMENT_FAILED);
            }

            log.info("[결제 승인 완료] orderId: {}", orderId);
        } catch (Exception e) {
            log.error("[결제 승인 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FINAL_STAGE_FAILED);
        }

        // [Tx-1.2] Outbox 상태 갱신 (complete) + SnapShot 상태 갱신 (ORDERED)
        return executeStatusUpdate(order);
    }


    /**
     * [Lua Atomic] 재고/쿠폰 예약 + Redis Streams Outbox 발행
     *
     * Lua Script를 사용하여 원자적으로 처리:
     * 1. 멱등성 체크
     * 2. 재고 차감 예약 (bf:stock:{productId} 형태)
     * 3. 쿠폰 사용 예약
     * 4. Redis Streams Outbox에 이벤트 발행
     */
    private String publishOrderCreatedEvent(Order order, CreateOrderCommand command) {
        try {
            // 이벤트 페이로드 생성
            OutboxEventPayload eventPayload = OutboxEventPayload.builder()
                    .orderId(order.getId())
                    .idempotencyKey(command.idempotencyKey())
                    .userId(command.userId())
                    .organizationId(command.organizationId())
                    .receiverCompanyId(order.getReceiverCompanyId())
                    .requestMessage(command.requestMessage())
                    .couponId(command.couponId())
                    .orderProducts(command.orderProducts())
                    .totalPrice(order.getTotalPrice())
                    .tossPaymentKey(command.tossPaymentKey())
                    .tossOrderId(command.tossOrderId())
                    .timestamp(LocalDateTime.now())
                    .build();

            // 상품 정보 매핑 (Lua Script용)
            List<Map<String, Object>> productList = command.orderProducts().stream()
                    .map(p -> {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("productId", p.productId().toString());
                        productMap.put("quantity", p.quantity());
                        return productMap;
                    })
                    .collect(Collectors.toList());

            // Lua Script 실행
            String messageId = redisOutboxPublisher.publishWithLuaScript(
                    order.getId(),
                    command.idempotencyKey(),
                    productList,
                    command.couponId(),
                    eventPayload
            );

            return messageId;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("[Lua Atomic 실패] orderId: {}, error: {}", order.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
