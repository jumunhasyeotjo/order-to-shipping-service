package com.jumunhasyeotjo.order_to_shipping.order.blackfriday.applicatiion;

import com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric;
import com.jumunhasyeotjo.order_to_shipping.order.application.CreateOrderSnapshotDto;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BFOrderOrchestrator {

    private final BFOrderService bfOrderService;



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

        return bfOrderService.order(command, pendingOrder, orderId, snapshotDto, status);
    }
}