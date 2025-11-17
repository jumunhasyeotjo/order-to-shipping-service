package com.jumunhasyeotjo.order_to_shipping.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.OrderProductReq;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.CreateOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.OrderUpdateStatusReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 생성 API")
    void createOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        // given
        List<OrderProductReq> orderProducts = new ArrayList<>();
        orderProducts.add(new OrderProductReq(UUID.randomUUID(), 1));

        CreateOrderReq request = new CreateOrderReq(1000, "요청사항", orderProducts);

        Order response = getOrder();

        given(orderService.createOrder(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/v1/orders")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value(OrderStatus.PENDING.toString()));
    }

    @Test
    @DisplayName("주문 상태 변경 API")
    void updateOrderStatus_ValidRequest_ReturnsUpdatedOrder() throws Exception {
        // given
        OrderUpdateStatusReq request = new OrderUpdateStatusReq(OrderStatus.DONE);

        Order response = getOrder();
        ReflectionTestUtils.setField(response, "status", OrderStatus.DONE);

        given(orderService.updateOrderStatus(any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/v1/orders/{orderId}", UUID.randomUUID())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(OrderStatus.DONE.toString()));
    }

    @Test
    @DisplayName("주문 취소 API")
    void cancelOrder_ValidOrderId_ReturnsCancelledOrder() throws Exception {
        // given
        Order response = getOrder();
        ReflectionTestUtils.setField(response, "status", OrderStatus.CANCELLED);

        given(orderService.cancelOrder(any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/v1/orders/{orderId}", UUID.randomUUID())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(OrderStatus.CANCELLED.toString()));
    }

    @Test
    @DisplayName("주문 단건 조회 API")
    void getOrder_ValidOrderId_ReturnsOrder() throws Exception {
        // given
        Order order = getOrder();
        OrderResult response = OrderResult.of(order);

        given(orderService.getOrder(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders/{orderId}", UUID.randomUUID())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestMessage").value(order.getRequestMessage()))
                .andExpect(jsonPath("$.data.totalPrice").value(order.getTotalPrice()))
                .andExpect(jsonPath("$.data.status").value(order.getStatus().toString()))
                .andExpect(jsonPath("$.data.orderProducts", hasSize(1)))
                .andExpect(jsonPath("$.data.orderProducts[0].name").value(order.getOrderProducts().get(0).getName()))
                .andExpect(jsonPath("$.data.orderProducts[0].price").value(order.getOrderProducts().get(0).getPrice()));
    }

    @Test
    @DisplayName("주문 다건 조회 API")
    void searchOrder_ValidParams_ReturnsOrderPage() throws Exception {
        // given
        OrderResult orderResult1 = OrderResult.of(getOrder());
        OrderResult orderResult2 = OrderResult.of(getOrder());
        OrderResult orderResult3 = OrderResult.of(getOrder());

        Page<OrderResult> response = new PageImpl<>(List.of(orderResult1, orderResult2, orderResult3),
                PageRequest.of(0 , 10), 0);

        given(orderService.searchOrder(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/orders")
                        .param("companyId", String.valueOf(UUID.randomUUID()))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3));
    }

    private Order getOrder() {
        List<OrderProduct> products = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        products.add(OrderProduct.create(productId, 1000, 1, "상품1"));
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;
        return Order.create(products, 1L, companyId, requestMessage, totalPrice);
    }
}
