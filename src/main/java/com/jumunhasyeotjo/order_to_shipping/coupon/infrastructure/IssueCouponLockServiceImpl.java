package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.service.IssueCouponLockService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IssueCouponLockServiceImpl implements IssueCouponLockService {

    private final RedissonClient redissonClient;

    private static final String LOCK_KEY = "coupon:lock:%s";

    @Override
    public <T> T executeWithLock(UUID couponId, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(String.format(LOCK_KEY, couponId));
        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new IllegalStateException("현재 요청이 몰려 잠시 후 다시 시도해주세요.");
            }

            return callback.call();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
