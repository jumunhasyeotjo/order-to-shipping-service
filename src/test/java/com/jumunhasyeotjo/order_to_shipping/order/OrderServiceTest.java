package com.jumunhasyeotjo.order_to_shipping.order;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.ProductResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.CompanyClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.ProductClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.StockClient;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.UserClient;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.repository.OrderRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Service 단위 테스트
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private StockClient stockClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    /** 생성 **/
    @Test
    @DisplayName("주문 생성은 존재하는 수령 업체만 가능하다.")
    void createOrder_whenCompanyDoesNotExist_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        UUID companyId = UUID.randomUUID();

        given(userClient.getCompanyId(request.userId()))
                .willReturn(Optional.of(companyId));
        given(companyClient.existCompany(companyId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 업체 입니다.");
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문 생성은 해당 주문상품 정보가 존재해야 가능하다.")
    void createOrder_whenProductInfoDoesNotExist_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        UUID companyId = UUID.randomUUID();
        List<ProductResult> productResults = new ArrayList<>();

        given(userClient.getCompanyId(request.userId()))
                .willReturn(Optional.of(companyId));
        given(companyClient.existCompany(companyId))
                .willReturn(true);
        given(productClient.findAllProducts(request.orderProducts()))
                .willReturn(productResults);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 상품 입니다.");
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문 생성은 재고가 부족하면 실패한다.")
    void createOrder_whenStockIsInsufficient_shouldThrowException() {
        // given
        CreateOrderCommand request = getCreateOrderCommand();
        UUID companyId = UUID.randomUUID();
        List<ProductResult> productResults = new ArrayList<>();
        ProductResult product = new ProductResult(request.orderProducts().get(0).productId(), "상품1", 1000, request.orderProducts().get(0).quantity());
        productResults.add(product);

        given(userClient.getCompanyId(request.userId()))
                .willReturn(Optional.of(companyId));
        given(companyClient.existCompany(companyId))
                .willReturn(true);
        given(productClient.findAllProducts(request.orderProducts()))
                .willReturn(productResults);
        given(stockClient.decreaseStock(request.orderProducts()))
                .willReturn(false);


        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문한 상품의 재고가 부족합니다.");
        verify(orderRepository, never()).save(any());
    }


    /** 수정 **/
    @Test
    @DisplayName("주문 수정은 해당 상품 주문 정보가 존재해야 가능하다.")
    void updateOrder_whenProductInfoDoesNotExist_shouldThrowException() {
        // given
        Order order = getOrder();
        UpdateOrderCommand request = getUpdateOrderCommand(order);
        List<ProductResult> productResults  = new ArrayList<>();

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(productClient.findAllProducts(request.orderProducts()))
                .willReturn(productResults);

        // when & then
        assertThatThrownBy(() -> orderService.updateOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 상품 입니다.");
    }

    @Test
    @DisplayName("주문 수정시 재고가 부족하면 실패한다.")
    void updateOrder_whenStockIsInsufficient_shouldThrowException() {
        Order order = getOrder();
        UpdateOrderCommand request = getUpdateOrderCommand(order);
        List<ProductResult> productResults  = new ArrayList<>();
        ProductResult product = new ProductResult(request.orderProducts().get(0).productId(), "상품1", 1000, request.orderProducts().get(0).quantity());
        productResults.add(product);

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(productClient.findAllProducts(request.orderProducts()))
                .willReturn(productResults);
        given(stockClient.decreaseStock(request.orderProducts()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.updateOrder(request))
                .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("주문한 상품의 재고가 부족합니다.");
    }

    @Test
    @DisplayName("마스터는 주문상태 변경이 가능하다")
    void updateOrderStatus_whenUserIsMaster_shouldSucceed() {
        Order order = getOrder();
        OrderUpdateStatusCommand request = new OrderUpdateStatusCommand(1L, order.getId(),"MASTER", OrderStatus.SHIPPED);

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));

        // when
        Order updateOrderStatus = orderService.updateOrderStatus(request);

        // then
        assertThat(updateOrderStatus.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("소속 허브 담당자만 주문상태 변경이 가능하다.")
    void updateOrderStatus_whenHubManagerIsNotInCorrectHub_shouldThrowException() {
        Order order = getOrder();
        OrderUpdateStatusCommand request = new OrderUpdateStatusCommand(1L, order.getId(),"HUB_MANAGER", OrderStatus.SHIPPED);
        UUID hubId = UUID.randomUUID();

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderStatus(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 소속 허브만 접근 가능합니다.");
    }

    /** 삭제 **/
    @Test
    @DisplayName("마스터는 주문 취소가 가능하다")
    void cancelOrder_whenUserIsMaster_shouldSucceed() {
        // given
        Order order = getOrder();
        CancelOrderCommand request = new CancelOrderCommand(1L, order.getId(), "MASTER");

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(request);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("소속 허브 담당자는 주문 취소가 가능하다")
    void cancelOrder_whenUserIsHubManager_shouldSucceed() {
        // given
        Order order = getOrder();
        UUID hubId = UUID.randomUUID();
        CancelOrderCommand request = new CancelOrderCommand(1L, order.getId(), "HUB_MANAGER");

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                .willReturn(true);

        // when
        orderService.cancelOrder(request);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("소속 허브 담당자만 주문 취소가 가능하다")
    void cancelOrder_whenHubManagerIsNotInCorrectHub_shouldThrowException() {
        // given
        Order order = getOrder();
        CancelOrderCommand request = new CancelOrderCommand(1L, order.getId(), "HUB_MANAGER");
        UUID hubId = UUID.randomUUID();

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 소속 허브만 접근 가능합니다.");
    }

    /** 조회 **/
    @Test
    @DisplayName("조회할 주문 미존재 (단건)")
    void getOrder_whenOrderDoesNotExist_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), 1L, "COMPANY_MANAGER");

        given(orderRepository.findById(order.getId()))
                .willReturn(Optional.empty());

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
        GetOrderCommand request = new GetOrderCommand(order.getId(), 1L, "COMPANY_MANAGER");

        given(orderRepository.findById(order.getId()))
                .willReturn(Optional.of(order));

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
        assertThat(order.getTotalPrice()).isEqualTo(result.totalPrice());
        assertThat(order.getRequestMessage()).isEqualTo(result.requestMessage());
        assertThat(order.getOrderProducts().get(0).getName()).isEqualTo(result.orderProducts().get(0).name());
    }

    @Test
    @DisplayName("업체 담당자는 본인이 주문한 상품만 조회 가능하다 - 실패. (단건)")
    void getOrder_whenCompanyManagerIsNotOwner_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), 2L, "COMPANY_MANAGER");

        given(orderRepository.findById(order.getId()))
                .willReturn(Optional.of(order));

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
        GetOrderCommand request = new GetOrderCommand(order.getId(), 2L, "HUB_MANAGER");
        UUID hubId = UUID.randomUUID();

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
                .willReturn(true);

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
        assertThat(order.getTotalPrice()).isEqualTo(result.totalPrice());
        assertThat(order.getRequestMessage()).isEqualTo(result.requestMessage());
        assertThat(order.getOrderProducts().get(0).getName()).isEqualTo(result.orderProducts().get(0).name());
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다 - 실패. (단건)")
    void getOrder_whenHubManagerIsInWrongHub_shouldThrowException() {
        // given
        Order order = getOrder();
        GetOrderCommand request = new GetOrderCommand(order.getId(), 2L, "HUB_MANAGER");
        UUID hubId = UUID.randomUUID();

        given(orderRepository.findById(request.orderId()))
                .willReturn(Optional.of(order));
        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(order.getCompanyId(), hubId))
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
        GetOrderCommand request = new GetOrderCommand(order.getId(), 1L, "MASTER");

        given(orderRepository.findById(order.getId()))
                .willReturn(Optional.of(order));

        // when
        OrderResult result = orderService.getOrder(request);

        // then
        assertThat(order.getId()).isEqualTo(result.orderId());
        assertThat(order.getTotalPrice()).isEqualTo(result.totalPrice());
        assertThat(order.getRequestMessage()).isEqualTo(result.requestMessage());
        assertThat(order.getOrderProducts().get(0).getName()).isEqualTo(result.orderProducts().get(0).name());
    }

    @Test
    @DisplayName("조회할 주문 미존재 (다건)")
    void searchOrder_whenNoOrdersFound_shouldReturnEmptyList() {
        // given
        Page<Order> orderList = new PageImpl<>(List.of(), PageRequest.of(0 , 10), 0);
        SearchOrderCommand request = new SearchOrderCommand(1L, UUID.randomUUID(), "MASTER", PageRequest.of(0, 10));

        given(orderRepository.findAllByCompanyId(request.companyId(), request.pageable()))
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
        Order order1 = getOrder();
        Order order2 = getOrder();
        Order order3 = getOrder();
        Page<Order> orderList = new PageImpl<>(List.of(order1, order2, order3),
                PageRequest.of(0 , 10), 0);

        SearchOrderCommand request = new SearchOrderCommand(1L, UUID.randomUUID(), "COMPANY_MANAGER", PageRequest.of(0, 10));

        given(userClient.getCompanyId(request.userId()))
                .willReturn(Optional.ofNullable(request.companyId()));
        given(orderRepository.findAllByCompanyId(request.companyId(), request.pageable()))
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
        UUID anotherCompanyId = UUID.randomUUID();
        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), "COMPANY_MANAGER", PageRequest.of(0, 10));

        given(userClient.getCompanyId(request.userId()))
                .willReturn(Optional.of(anotherCompanyId));

        // when & then
        assertThatThrownBy(() -> orderService.searchOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 조회할 권한이 없습니다");
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다. - 성공 (다건)")
    void searchOrder_whenHubManagerIsInCorrectHub_shouldSucceed() {
        // given
        Order order1 = getOrder();
        Order order2 = getOrder();
        Order order3 = getOrder();
        Page<Order> orderList = new PageImpl<>(List.of(order1, order2, order3),
                PageRequest.of(0 , 10), 0);

        UUID hubId = UUID.randomUUID();
        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), "HUB_MANAGER", PageRequest.of(0, 10));

        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(request.companyId(), hubId))
                .willReturn(true);
        given(orderRepository.findAllByCompanyId(request.companyId(), request.pageable()))
                .willReturn(orderList);

        // when
        Page<OrderResult> results = orderService.searchOrder(request);

        // then
        assertThat(results.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("허브 담당자는 본인 소속 주문만 조회 가능하다. - 실패 (다건)")
    void searchOrder_whenHubManagerIsInWrongHub_shouldThrowException() {
        // given
        UUID hubId = UUID.randomUUID();
        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), "HUB_MANAGER", PageRequest.of(0, 10));

        given(userClient.getHubId(request.userId()))
                .willReturn(Optional.of(hubId));
        given(companyClient.existCompanyRegionalHub(request.companyId(), hubId))
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
        Order order1 = getOrder();
        Order order2 = getOrder();
        Order order3 = getOrder();
        Page<Order> orderList = new PageImpl<>(List.of(order1, order2, order3),
                PageRequest.of(0 , 10), 0);

        SearchOrderCommand request = new SearchOrderCommand(2L, UUID.randomUUID(), "MASTER", PageRequest.of(0, 10));

        given(orderRepository.findAllByCompanyId(request.companyId(), request.pageable()))
                .willReturn(orderList);

        // when
        Page<OrderResult> results = orderService.searchOrder(request);

        // then
        assertThat(results.getContent().size()).isEqualTo(3);
    }


    // 헬퍼 메서드
    private Order getOrder() {
        List<OrderProduct> products = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        products.add(OrderProduct.create(productId, 1000, 1, "상품1"));
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;
        return Order.create(products, 1L, companyId, requestMessage, totalPrice);
    }

    private CreateOrderCommand getCreateOrderCommand() {
        int totalPrice = 10000;
        String requestMessage = "요구사항";
        List<OrderProductReq> orderProducts = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        int quantity = 10;
        orderProducts.add(new OrderProductReq(productId, quantity));

        return new CreateOrderCommand(1L, totalPrice, requestMessage, orderProducts);
    }

    private static UpdateOrderCommand getUpdateOrderCommand(Order order) {
        int totalPrice = 10000;
        String requestMessage = "주문수정";
        List<OrderProductReq> orderProducts = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        int quantity = 10;
        orderProducts.add(new OrderProductReq(productId, quantity));
        return new UpdateOrderCommand(1L, order.getId(), totalPrice, requestMessage, orderProducts);
    }
}
