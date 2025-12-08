package com.jumunhasyeotjo.order_to_shipping.coupon.application;


import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.IssueCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.event.CouponIssueEvent;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.result.IssueCouponResult;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.CouponEventProducerService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.CouponStockService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.IssueCouponLockService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.IssueCouponQueueService;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.CouponEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueCouponOrchestratorService {
    private final CouponService couponService;
    private final IssueCouponService issueCouponService;
    private final IssueCouponQueueService issueCouponQueueService;
    private final IssueCouponLockService issueCouponLockService;
    private final CouponStockService couponStockService;
    private final CouponEventProducerService couponEventProducer;

    /*
    * 여기는 Client에서 Polling 방식으로 Redis 대기열과 Redis Lock을 이용하는 쿠폰 발급 방식이다.
     */
    public void initStock(UUID couponId) {
        Integer maxQuantity = couponService.getCoupon(couponId).maxQuantity();
        couponStockService.setInitialStock(couponId, maxQuantity);
    }

    // 1. 대기열 진입
    public Long joinQueue(IssueCouponCommand command) {
        ensureStockAvailable(command.couponId());
        return issueCouponQueueService.joinQueue(command.couponId(), command.userId());
    }

    // 2. Polling 방식으로 확인.
    public Long getUserPosition(IssueCouponCommand command) {
        ensureStockAvailable(command.couponId());
        return issueCouponQueueService.getUserPosition(command.couponId(), command.userId());
    }

    // 3. 1인 경우 발급
    public IssueCouponResult issueCoupon(IssueCouponCommand command) {
        ensureFirstPosition(command);

        return issueCouponLockService.executeWithLock(command.couponId(), () -> {
            ensureFirstPosition(command);
            ensureStockAvailable(command.couponId());
            IssueCouponResult result = issueCouponService.issue(command);
            couponStockService.decrease(command.couponId());
            issueCouponQueueService.popFirstUser(command.couponId());
            return result;
        });
    }

    private void ensureStockAvailable(UUID couponId) {
        if (couponStockService.getStock(couponId) <= 0) {
            throw new IllegalArgumentException("쿠폰이 전부 소진되었습니다.");
        }
    }

    private void ensureFirstPosition(IssueCouponCommand command) {
        Long position = issueCouponQueueService.getUserPosition(command.couponId(), command.userId());
        if (position != 1) {
            throw new RuntimeException("현재 대기 순서는 " + position + "이므로 발급할 수 없습니다.");
        }
    }

    /*
     * 여기는 Kafka와 Redis 재고 관리를 통해 비동기적으로 DB에 저장하는 쿠폰 발급 방식이다.
     */
    public void issueCouponWithKafka(IssueCouponCommand command) {
        UUID couponId = command.couponId();
        Long userId = command.userId();

        Boolean exists = couponStockService.exists(couponId, userId);
        if (exists != null && exists) {
            throw new IllegalArgumentException("이미 쿠폰을 발급한 유저입니다.");
        }

        Long stock = couponStockService.decrease(couponId);
        if (stock == null || stock < 0) {
            couponStockService.increase(couponId);
            throw new RuntimeException("쿠폰이 전부 소진됐습니다.");
        }

        couponEventProducer.sendIssueEvent(new CouponIssueEvent(couponId, userId));

        couponStockService.issue(couponId, userId);
    }
}
