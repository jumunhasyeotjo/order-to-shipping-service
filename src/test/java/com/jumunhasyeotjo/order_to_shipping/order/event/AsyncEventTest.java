package com.jumunhasyeotjo.order_to_shipping.order.event;

import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.payment.application.event.PaymentEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getCreateOrderCommand;
import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getProductResults;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AsyncEventTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private CompanyClient companyClient;

    @MockitoBean
    private StockClient stockClient;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private PaymentEventListener paymentEventListener;

    @Autowired
    private OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(AsyncEventTest.class);

    CreateOrderCommand request = getCreateOrderCommand();

    @Test
    @DisplayName("이벤트 비동기 처리")
    void events_ShouldBeProcessedAsynchronously() {
        // given
        setupDefaultMocks();
        log.info("메인 스레드 : {}", Thread.currentThread().getName());

        // when & then
        orderService.createOrder(request); // 이벤트 발행
        await()
                .atMost(Duration.ofSeconds(3)) // 메인 스레드 3초 대기 설정
                .pollInterval(Duration.ofSeconds(1)) // 1 초마다 확인
                .untilAsserted(() -> {
                    verify(paymentEventListener, times(1))
                            .handleOrderCreatedAsync(any(OrderCreatedEvent.class));
                });
    }

    @Test
    @DisplayName("비동기 처리 예외 발생시 메인 로직은 영향을 받지않는다")
    void whenAsyncEventThrowsException_MainFlowShouldNotBeAffected() {
        // given
        setupDefaultMocks();

        // 비동기 메서드 예외 발생
        doThrow(new RuntimeException("결제 실패"))
                .when(paymentEventListener)
                .handleOrderCreatedAsync(any(OrderCreatedEvent.class));

        // when & then
        assertThatCode(() -> orderService.createOrder(request))
                .doesNotThrowAnyException();
    }

    // 테스트 헬퍼 메서드
    private void setupDefaultMocks() {
        given(userClient.getCompanyId(request.userId())).willReturn(Optional.of(UUID.randomUUID()));
        given(companyClient.existCompany(any())).willReturn(true);
        given(productClient.findAllProducts(any())).willReturn(getProductResults(request));
        given(stockClient.decreaseStock(any())).willReturn(true);
    }
}
