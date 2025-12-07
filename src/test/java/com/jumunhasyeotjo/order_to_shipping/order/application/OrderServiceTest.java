package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.*;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderFixtures.getOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Service 단위 테스트
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderCompanyClient orderCompanyClient;

    @Mock
    private OrderStockClient orderStockClient;

    @Mock
    private OrderProductClient orderProductClient;

    @Mock
    private OrderCouponClient orderCouponClient;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private OrderPersistenceService orderPersistenceService;

    @InjectMocks
    private OrderService orderService;

    /**
     * 생성
     **/
    @Test
    @DisplayName("주문 생성은 존재하는 수령 업체만 가능하다.")
    void createOrder_whenCompanyDoesNotExist_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();

        given(orderCompanyClient.existCompany(request.organizationId()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 업체 입니다.");
        verify(orderPersistenceService, never()).saveOrder(any(), any(), anyInt());
    }

    @Test
    @DisplayName("주문 생성은 해당 주문상품 정보가 존재해야 가능하다.")
    void createOrder_whenProductInfoDoesNotExist_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        List<ProductResult> productResults = new ArrayList<>();

        given(orderPersistenceService.existsByIdempotencyKey(request.idempotencyKey()))
                .willReturn(false);
        given(orderCompanyClient.existCompany(request.organizationId()))
                .willReturn(true);
        given(orderProductClient.findAllProducts(any()))
                .willReturn(productResults);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 상품 입니다.");
        verify(orderPersistenceService, never()).saveOrder(any(), any(), anyInt());
    }

    @Test
    @DisplayName("주문 생성은 재고가 부족하면 실패한다.")
    void createOrder_whenStockIsInsufficient_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        List<ProductResult> productResults = new ArrayList<>();
        ProductResult product = new ProductResult(request.orderProducts().get(0).productId(),
                UUID.randomUUID(),
                "상품1",
                1000);
        productResults.add(product);

        given(orderCompanyClient.existCompany(request.organizationId())).willReturn(true);
        given(orderPersistenceService.existsByIdempotencyKey(request.idempotencyKey())).willReturn(false);
        given(orderProductClient.findAllProducts(any())).willReturn(productResults);
        given(orderPersistenceService.saveOrder(any(), any(), anyInt())).willReturn(getOrder());
        given(orderCouponClient.useCoupon(any(), any())).willReturn(true);
        given(orderStockClient.decreaseStock(any(), any())).willReturn(false); // 재고 부족

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문한 상품의 재고가 부족합니다.");
    }


    /**
     * 수정
     **/
    @Test
    @DisplayName("마스터는 주문상태 변경이 가능하다")
    void updateOrderStatus_whenUserIsMaster_shouldSucceed() {
        Order order = getOrder();
        OrderUpdateStatusCommand request = new OrderUpdateStatusCommand(1L, UUID.randomUUID(), order.getId(), "MASTER", OrderStatus.SHIPPED);

        given(orderPersistenceService.findById(request.orderId()))
                .willReturn(order);

        ReflectionTestUtils.setField(order, "status", request.status());
        given(orderPersistenceService.updateOrderStatus(any(), any(), any(), any()))
                .willReturn(order);

        // when
        Order result = orderService.updateOrderStatus(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("소속 허브 담당자만 주문상태 변경이 가능하다.")
    void updateOrderStatus_whenHubManagerIsNotInCorrectHub_shouldThrowException() {
        Order order = getOrder();
        OrderUpdateStatusCommand request = new OrderUpdateStatusCommand(1L, UUID.randomUUID(), order.getId(), "HUB_MANAGER", OrderStatus.SHIPPED);

        given(orderPersistenceService.findById(request.orderId()))
                .willReturn(order);
        given(orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), request.organizationId()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderStatus(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 소속 허브만 접근 가능합니다.");
    }

    /**
     * 삭제
     **/
    @Test
    @DisplayName("마스터는 주문 취소가 가능하다")
    void cancelOrder_whenUserIsMaster_shouldSucceed() {
        // given
        Order order = getOrder();
        CancelOrderCommand request = new CancelOrderCommand(1L, UUID.randomUUID(), order.getId(), "MASTER");

        given(orderPersistenceService.findById(request.orderId()))
                .willReturn(order);
        ReflectionTestUtils.setField(order, "status", OrderStatus.CANCELLED);
        given(orderPersistenceService.cancelOrder(any(), any())).willReturn(order);

        // when
        Order result = orderService.cancelOrder(request);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderPersistenceService).cancelOrder(any(), eq(request));
    }

    @Test
    @DisplayName("소속 허브 담당자는 주문 취소가 가능하다")
    void cancelOrder_whenUserIsHubManager_shouldSucceed() {
        // given
        Order order = getOrder();
        CancelOrderCommand request = new CancelOrderCommand(1L, UUID.randomUUID(), order.getId(), "HUB_MANAGER");

        given(orderPersistenceService.findById(request.orderId()))
                .willReturn(order);
        given(orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), request.organizationId()))
                .willReturn(true);
        ReflectionTestUtils.setField(order, "status", OrderStatus.CANCELLED);
        given(orderPersistenceService.cancelOrder(any(), any())).willReturn(order);

        // when
        Order result = orderService.cancelOrder(request);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderPersistenceService).cancelOrder(any(), eq(request));
    }

    @Test
    @DisplayName("소속 허브 담당자만 주문 취소가 가능하다")
    void cancelOrder_whenHubManagerIsNotInCorrectHub_shouldThrowException() {
        // given
        Order order = getOrder();
        CancelOrderCommand request = new CancelOrderCommand(1L, UUID.randomUUID(), order.getId(), "HUB_MANAGER");

        given(orderPersistenceService.findById(request.orderId()))
                .willReturn(order);
        given(orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), request.organizationId()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 소속 허브만 접근 가능합니다.");
    }

    /**
     * 조회
     **/
    @Test
    @DisplayName("조회할 주문 미존재 (단건)")
    void getOrder_whenOrderDoesNotExist_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), UUID.randomUUID(), 1L, "COMPANY_MANAGER");

        given(orderPersistenceService.findByIdWithAll(order.getId()))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 주문 입니다.");
    }

    @Test
    @DisplayName("업체 담당자는 본인이 주문한 상품만 조회 가능하다 - 성공. (단건)")
    void getOrder_whenCompanyManagerIsOwner_shouldSucceed() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(),UUID.randomUUID(), 1L, "COMPANY_MANAGER");

        given(orderPersistenceService.findByIdWithAll(order.getId()))
                .willReturn(order);

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
        assertThat(order.getTotalPrice()).isEqualTo(result.totalPrice());
    }

    @Test
    @DisplayName("수령 업체 담당자는 본인이 주문한 상품만 조회 가능하다 - 실패. (단건)")
    void getOrder_whenCompanyManagerIsNotOwner_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), UUID.randomUUID(), 2L, "COMPANY_MANAGER");

        given(orderPersistenceService.findByIdWithAll(order.getId()))
                .willReturn(order);

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 조회할 권한이 없습니다");
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다 - 성공. (단건)")
    void getOrder_whenHubManagerIsInCorrectHub_shouldSucceed() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), UUID.randomUUID(),2L, "HUB_MANAGER");

        given(orderPersistenceService.findByIdWithAll(request.orderId()))
                .willReturn(order);
        given(orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), request.organizationId()))
                .willReturn(true);

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다 - 실패. (단건)")
    void getOrder_whenHubManagerIsInWrongHub_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), UUID.randomUUID(), 2L, "HUB_MANAGER");

        given(orderPersistenceService.findByIdWithAll(request.orderId()))
                .willReturn(order);
        given(orderCompanyClient.existCompanyRegionalHub(order.getReceiverCompanyId(), request.organizationId()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 조회할 권한이 없습니다");
    }

    @Test
    @DisplayName("마스터는 모든 주문 정보를 조회 가능하다. (단건)")
    void getOrder_whenUserIsMaster_shouldSucceed() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), UUID.randomUUID(), 1L, "MASTER");

        given(orderPersistenceService.findByIdWithAll(order.getId()))
                .willReturn(order);

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
    }

    @Test
    @DisplayName("조회할 주문 미존재 (다건)")
    void searchOrder_whenNoOrdersFound_shouldReturnEmptyList() {
        // given
        Page<OrderResult> orderList = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        SearchOrderCommand request = new SearchOrderCommand(1L, UUID.randomUUID(), UUID.randomUUID(), "MASTER", PageRequest.of(0, 10));

        given(orderPersistenceService.searchOrder(request))
                .willReturn(orderList);

        // when
        Page<OrderResult> result = orderService.searchOrder(request);

        // then
        assertThat(result.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("업체 담당자는 본인이 주문한 상품만 조회 가능하다. - 성공 (다건)")
    void searchOrder_whenCompanyManagerIsOwner_shouldSucceed() {
        // given
        OrderResult result1 = OrderResult.of(getOrder());
        OrderResult result2 = OrderResult.of(getOrder());
        OrderResult result3 = OrderResult.of(getOrder());
        UUID companyId = UUID.randomUUID();

        Page<OrderResult> orderList = new PageImpl<>(List.of(result1, result2, result3),
                PageRequest.of(0, 10), 0);

        SearchOrderCommand request = new SearchOrderCommand(1L, companyId, companyId, "COMPANY_MANAGER", PageRequest.of(0, 10));

        given(orderPersistenceService.searchOrder(request))
                .willReturn(orderList);

        // when
        Page<OrderResult> results = orderService.searchOrder(request);

        // then
        assertThat(results.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("업체 담당자는 본인이 주문한 상품만 조회 가능하다. - 실패 (다건)")
    void searchOrder_whenCompanyManagerIsNotOwner_shouldThrowException() {
        // given
        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), UUID.randomUUID(),"COMPANY_MANAGER", PageRequest.of(0, 10));

        // when & then
        assertThatThrownBy(() -> orderService.searchOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 조회할 권한이 없습니다");
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다. - 성공 (다건)")
    void searchOrder_whenHubManagerIsInCorrectHub_shouldSucceed() {
        // given
        OrderResult result1 = OrderResult.of(getOrder());
        Page<OrderResult> orderList = new PageImpl<>(List.of(result1), PageRequest.of(0, 10), 0);

        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), UUID.randomUUID(),"HUB_MANAGER", PageRequest.of(0, 10));

        given(orderCompanyClient.existCompanyRegionalHub(request.companyId(), request.organizationId()))
                .willReturn(true);
        given(orderPersistenceService.searchOrder(request))
                .willReturn(orderList);

        // when
        Page<OrderResult> results = orderService.searchOrder(request);

        // then
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다. - 실패 (다건)")
    void searchOrder_whenHubManagerIsInWrongHub_shouldThrowException() {
        // given
        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), UUID.randomUUID(), "HUB_MANAGER", PageRequest.of(0, 10));

        given(orderCompanyClient.existCompanyRegionalHub(request.companyId(), request.organizationId()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.searchOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 조회할 권한이 없습니다");
    }

    @Test
    @DisplayName("마스터는 모든 주문 정보를 조회 가능하다. (다건)")
    void searchOrder_whenUserIsMaster_shouldSucceed() {
        // given
        OrderResult result1 = OrderResult.of(getOrder());
        Page<OrderResult> orderList = new PageImpl<>(List.of(result1), PageRequest.of(0, 10), 0);

        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), UUID.randomUUID(), "MASTER", PageRequest.of(0, 10));

        given(orderPersistenceService.searchOrder(request))
                .willReturn(orderList);

        // when
        Page<OrderResult> results = orderService.searchOrder(request);

        // then
        assertThat(results.getContent().size()).isEqualTo(1);
    }

    // 헬퍼 메서드
    private CreateOrderCommand getCreateOrderCommand() {
        String requestMessage = "요구사항";
        List<OrderProductReq> orderProducts = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        int quantity = 10;
        orderProducts.add(new OrderProductReq(productId, quantity));

        return new CreateOrderCommand(1L, UUID.randomUUID(), requestMessage, orderProducts, "멱등키");
    }
}
