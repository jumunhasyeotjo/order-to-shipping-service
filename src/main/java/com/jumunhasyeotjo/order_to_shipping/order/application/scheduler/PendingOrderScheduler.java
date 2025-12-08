package com.jumunhasyeotjo.order_to_shipping.order.application.scheduler;

import com.jumunhasyeotjo.order_to_shipping.order.application.OrderPersistenceService;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PendingOrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderPersistenceService orderPersistenceService;

    private static final int BATCH_SIZE = 10;

    @Scheduled(cron = "0 */5 * * * *")
    public void rollbackPendingOrder() {
        Pageable pageable = PageRequest.of(0, BATCH_SIZE, Sort.by("createdAt").ascending());
        List<Order> pendingOrderList = orderRepository.findAllByStatus(OrderStatus.PENDING, pageable);

        if (pendingOrderList.isEmpty()) {
            return;
        }

        for (Order order : pendingOrderList) {
            try {
                orderPersistenceService.rollbackSingleOrder(order);
            } catch (Exception e) {
                log.error("서버 크래시 스케줄러 처리 실패 OrderId: {}", order.getId(), e);
            }
        }
    }
}
