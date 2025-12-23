package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BFOrderOutboxService {

    private final BfOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Outbox 생성
     *
     * Lua Script 실행 후 DB에도 기록
     * (Redis 장애 대비 이중화)
     */
    @Transactional
    public Outbox createOutbox(UUID orderId, String idempotencyKey,
                               String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            Outbox outbox = Outbox.builder()
                    .orderId(orderId)
                    .idempotencyKey(idempotencyKey)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .maxRetryCount(3)
                    .build();

            Outbox saved = outboxRepository.save(outbox);
            log.info("[Outbox 생성] id: {}, orderId: {}", saved.getId(), orderId);

            return saved;

        } catch (Exception e) {
            log.error("[Outbox 생성 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OUTBOX_PUBLISH_FAILED);
        }
    }

    /**
     * Outbox 상태를 'READY'로 변경
     *
     * Worker가 이벤트 처리 완료 후 호출
     */
//    @Transactional
//    public void updateOutboxStatusToReady(String orderId) {
//        try {
//            Outbox outbox = outboxRepository.findByOrderId(orderId)
//                    .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));
//
//            outbox.markAsReady();
//            outboxRepository.save(outbox);
//
//            log.info("[상태 변경: READY] outboxId: {}, orderId: {}", outbox.getId(), orderId);
//
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("[상태 변경 실패: READY] orderId: {}, error: {}", orderId, e.getMessage(), e);
//            throw new BusinessException(ErrorCode.OUTBOX_PROCESSING_FAILED);
//        }
//    }

    /**
     * Outbox 상태를 'COMPLETE'로 변경
     *
     * Final Stage에서 결제 승인 및 주문 확정 후 호출
     */
    @Transactional
    public void updateOutboxStatusToComplete(UUID orderId) {
        try {
            Outbox outbox = outboxRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));

            outbox.markAsComplete();
            outboxRepository.save(outbox);

            log.info("[상태 변경: COMPLETE] outboxId: {}, orderId: {}", outbox.getId(), orderId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[상태 변경 실패: COMPLETE] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FINAL_STAGE_FAILED);
        }
    }

    /**
     * Outbox 상태를 'FAILED'로 변경
     *
     * 처리 중 오류 발생 시 호출
     */
    @Transactional
    public void updateOutboxStatusToFailed(UUID orderId, String errorMessage) {
        try {
            Outbox outbox = outboxRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));

            outbox.markAsFailed(errorMessage);
            outboxRepository.save(outbox);

            log.error("[상태 변경: FAILED] outboxId: {}, orderId: {}, error: {}",
                    outbox.getId(), orderId, errorMessage);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[상태 변경 실패: FAILED] orderId: {}, error: {}", orderId, e.getMessage(), e);
            // 실패 처리 중 오류는 throw 하지 않음
        }
    }

//    /**
//     * Outbox 상태 조회
//     */
//    @Transactional(readOnly = true)
//    public String getOutboxStatus(String orderId) {
//        return outboxRepository.findByOrderId(orderId)
//                .map(outbox -> outbox.getStatus().name())
//                .orElse(null);
//    }
//
//    /**
//     * 재시도 횟수 증가
//     */
//    @Transactional
//    public void incrementRetryCount(String orderId) {
//        Outbox outbox = outboxRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));
//
//        outbox.incrementRetryCount();
//        outboxRepository.save(outbox);
//
//        log.info("[재시도 횟수 증가] outboxId: {}, orderId: {}, retryCount: {}",
//                outbox.getId(), orderId, outbox.getRetryCount());
//    }

    /**
     * N번 이상 결과 나오지 않은 데이터 조회
     *
     * 스케줄러에서 주기적으로 호출하여 재처리
     *
     * @param minutesAgo 몇 분 이전 데이터부터 조회할지
     * @return 재처리 대상 Outbox 목록
     */
    @Transactional(readOnly = true)
    public List<Outbox> findPendingOutboxesForRetry(int minutesAgo) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesAgo);
        List<Outbox> outboxes = outboxRepository.findPendingOutboxesForRetry(threshold);

        log.info("[재처리 대상 조회] 개수: {}, threshold: {}", outboxes.size(), threshold);
        return outboxes;
    }

    /**
     * 실패한 Outbox 조회 (최대 재시도 횟수 초과)
     */
    @Transactional(readOnly = true)
    public List<com.jumunhasyeotjo.order_to_shipping.order.application.Outbox> findFailedOutboxes() {
        List<Outbox> outboxes = outboxRepository.findFailedOutboxes();
        log.info("[실패 데이터 조회] 개수: {}", outboxes.size());
        return outboxes;
    }

    /**
     * 완료된 Outbox 정리 (24시간 이상 지난 데이터)
     */
    @Transactional
    public void cleanupCompletedOutboxes() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Outbox> completedOutboxes = outboxRepository
                .findByStatusAndUpdatedAtBefore(Outbox.OutboxStatus.COMPLETE, threshold);

        if (!completedOutboxes.isEmpty()) {
            outboxRepository.deleteAll(completedOutboxes);
            log.info("[Outbox 정리] 삭제된 개수: {}", completedOutboxes.size());
        }
    }

    public void upsert(String orderId, String idempotencyKey,
                       String eventType, Object payload) {

    }

    @Transactional
    public void markAsReady(UUID orderId) {
        Outbox outbox = outboxRepository.findByOrderId(orderId).orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_INIT));
        outbox.markAsReady();
    }

    @Transactional
    public void setPayload(UUID orderId, String payload) {
        Outbox outbox = outboxRepository.findByOrderId(orderId).orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_INIT));
        outbox.setPayload(payload);
    }
}
