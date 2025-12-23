package com.jumunhasyeotjo.order_to_shipping.order.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BfOutboxRepository extends JpaRepository<Outbox, Long> {

    Optional<Outbox> findByOrderId(UUID orderId);

    Optional<Outbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * N번 이상 재시도했지만 아직 완료되지 않은 데이터 조회
     * 스케줄러에서 주기적으로 조회하여 재처리
     */
    @Query("SELECT o FROM Outbox o " +
            "WHERE o.status IN ('PENDING', 'PROCESSING') " +
            "AND o.retryCount < o.maxRetryCount " +
            "AND o.createdAt < :threshold " +
            "ORDER BY o.createdAt ASC")
    List<Outbox> findPendingOutboxesForRetry(@Param("threshold") LocalDateTime threshold);

    /**
     * 실패한 데이터 조회 (최대 재시도 횟수 초과)
     */
    @Query("SELECT o FROM Outbox o " +
            "WHERE o.status = 'PENDING' " +
            "AND o.retryCount >= o.maxRetryCount")
    List<Outbox> findFailedOutboxes();

    /**
     * 특정 시간 이전의 완료된 데이터 조회 (정리용)
     */
    List<Outbox> findByStatusAndUpdatedAtBefore(Outbox.OutboxStatus status, LocalDateTime threshold);
}
