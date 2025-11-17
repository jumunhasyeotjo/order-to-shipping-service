package com.jumunhasyeotjo.order_to_shipping.order.event;

import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.event.OrderEventListener;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;
import java.util.UUID;

import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getCreateOrderCommand;
import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getProductResults;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class OrderCreatedEventTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private CompanyClient companyClient;

    @MockitoBean
    private StockClient stockClient;

    @MockitoBean
    private ProductClient productClient;

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private OrderEventListener orderEventListener;

    CreateOrderCommand request = getCreateOrderCommand();

    @Test
    @DisplayName("주문 생성시 OrderCreatedEvent 는 정상 발행 된다.")
    void createOrder_shouldPublishOrderCreatedEvent() {
        // given
        setupDefaultMocks();

        // when
        orderService.createOrder(request);

        // then
        verify(orderEventListener, times(1))
                .handleOrderCreated(any(OrderCreatedEvent.class));
    }

    // 테스트 헬퍼 메서드
    private void setupDefaultMocks() {
        given(userClient.getCompanyId(request.userId())).willReturn(Optional.of(UUID.randomUUID()));
        given(companyClient.existCompany(any())).willReturn(true);
        given(productClient.findAllProducts(any())).willReturn(getProductResults(request));
        given(stockClient.decreaseStock(any())).willReturn(true);
    }
}