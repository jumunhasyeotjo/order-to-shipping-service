package com.jumunhasyeotjo.order_to_shipping.order.event;

import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.CreateOrderCommand;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.payment.application.PaymentService;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.entity.Payment;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.repository.PaymentRepository;
import com.jumunhasyeotjo.order_to_shipping.payment.domain.vo.PaymentStatus;
import com.jumunhasyeotjo.order_to_shipping.stock.domain.entity.Stock;
import com.jumunhasyeotjo.order_to_shipping.stock.domain.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getCreateOrderCommand;
import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderEventFixture.getProductResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class OrderFlowIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private CompanyClient companyClient;

    @MockitoBean
    private StockClient stockClient;

    @MockitoBean
    private ProductClient productClient;

    @MockitoSpyBean
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StockRepository stockRepository;

    static final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUpMock() {
        stockRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();

        Stock stock = Stock.builder()
                .productId(productId)
                .quantity(10)
                .build();

        stockRepository.save(stock);
    }

    @Test
    @DisplayName("주문 생성 -> 결제 완료 -> 재고 차감 정상 플로우")
    void fullOrderFlow_ShouldWorkCorrectly() throws InterruptedException {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        setUpMock(request);

        // when
        Order response = orderService.createOrder(request);
        Thread.sleep(3000);

        // then
        // 1. 주문 생성 확인
        Order order = orderRepository.findById(response.getId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // 2. 결제 확인
        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // 3. 재고 차감 확인
        Stock updateStock = stockRepository.findByProductId(productId).orElseThrow();
        assertThat(updateStock.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("주문 생성 -> 결제실패시 플로우")
    void whenPaymentFails_StockShouldNotDecrease() throws InterruptedException {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        setUpMock(request);

        doThrow(new RuntimeException("결제 실패"))
                .when(paymentService)
                .processPayment(any(), anyInt(), any());

        // when
        Order response = orderService.createOrder(request);
        Thread.sleep(3000);

        // then
        // 재고 확인
        Stock updateStock = stockRepository.findByProductId(productId).orElseThrow();
        assertThat(updateStock.getQuantity()).isEqualTo(10);
    }

    // 테스트 헬퍼 메서드
    public static CreateOrderCommand getCreateOrderCommand() {
        return new CreateOrderCommand(1L, 5000, "주문",
                new ArrayList<>(List.of(new OrderProductReq(productId, 5))));
    }

    private void setUpMock(CreateOrderCommand request) {
        given(userClient.getCompanyId(request.userId())).willReturn(Optional.of(UUID.randomUUID()));
        given(companyClient.existCompany(any())).willReturn(true);
        given(productClient.findAllProducts(any())).willReturn(getProductResults(request));
        given(stockClient.decreaseStock(any())).willReturn(true);
    }
}
