package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderPaymentClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BFOrderOrchestrator {

    private final OrderService orderService;
    private final BFOrderService bfOrderService;
    private final OrderPaymentClient orderPaymentClient;
    private final RedisOutboxPublisher redisOutboxPublisher;


    /**
     * 주문 생성 전체 프로세스
     *
     * 흐름:
     * 1. [Tx-1] Request SnapShot 저장 (Order PENDING) → 커밋
     * 2. [Lua Atomic] 재고/쿠폰 예약 + Outbox 발행
     * 3. [Worker 대기] Redis Outbox Worker가 이벤트 처리 완료 대기
     * 4. [Final Stage] 결제 승인 + Outbox/SnapShot 상태 갱신 → 커밋
     * 5. 사용자 응답 반환
     */
    @Metric("주문 생성 전체 프로세스")
    public Order createOrder(CreateOrderCommand command) {
        log.info("[주문 생성 시작] idempotencyKey: {}", command.idempotencyKey());

        // 1. [Tx-1] Request SnapShot 저장 - 별도 트랜잭션
        CreateOrderSnapshotDto snapshotDto = bfOrderService.createOrderSnapshot(command);
        Order pendingOrder = snapshotDto.getPendingOrder();
        UUID orderId = pendingOrder.getId();

        log.info("[Tx-1 완료] Order 저장 및 커밋 - orderId: {}, status: PENDING", orderId);
        RollbackStatus status = RollbackStatus.NONE;

        try {

            // 2. [Lua Atomic] 재고/쿠폰 예약 + Redis Streams Outbox 발행
            String messageId = publishOrderCreatedEvent(pendingOrder, command);
            log.info("[Lua Atomic 완료] Outbox 발행 - messageId: {}, orderId: {}", messageId, orderId);

            //해당 지점에서 Crash 발생시 스케줄러를 통해 publishOrderCreatedEvent 보상

            // 3. [Final Stage] 결제 승인 + 상태 갱신
            int paymentPrice = pendingOrder.getTotalPrice() - snapshotDto.getDiscountPrice();
            Order completedOrder = executeFinalStage(orderId, paymentPrice, command);
            log.info("[Final Stage 완료] 주문 완료 - orderId: {}, status: ORDERED", orderId);

            // 4. 응답 반환
            return completedOrder;

        } catch (Exception e) {
            log.error("[주문 생성 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            log.warn("[Compensate] 주문 실패 롤백 실행 - 단계: {}, 사유: {}", status, e.getMessage());
            orderService.updateOrderStatus(orderId, OrderStatus.FAILED, null, status);
            throw e;
        }
    }

    private Order executeFinalStage(UUID orderId, int totalPrice, CreateOrderCommand command) {
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
        return bfOrderService.executeStatusUpdate(orderId);
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
            log.error("[Lua Atomic 실패] orderId: {}, error: {}", order.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.OUTBOX_PUBLISH_FAILED);
        }
    }
}