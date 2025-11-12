package com.jumunhasyeotjo.order_to_shipping.order;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

// Domain 단위 테스트
public class OrderDomainTest {

    /** 생성 **/
    @Test
    @DisplayName("주문 생성시 상태는 PENDING 이어야 한다")
    void create_ShouldHavePendingStatus() {
        // given
        List<OrderProduct> products = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = Order.create(products, 1L, companyId, requestMessage, totalPrice);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderProducts().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(order.getRequestMessage()).isEqualTo(requestMessage);
        assertThat(order.getCompanyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("주문 생성시 주문 상품은 1개 이상이어야 한다")
    void create_withEmptyProductList_ShouldThrowException() {
        // given
        List<OrderProduct> orderProducts = new ArrayList<>();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 10000;

        // when & then
        assertThatThrownBy(() -> Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 상품은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 생성시 총 가격은 주문 상품들의 가격의 총합과 동일해야된다")
    void create_whenTotalPriceMismatchesSumOfProducts_shouldThrowException(){
        // given
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 100;

        // when & then
        assertThatThrownBy(() -> Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("총 가격은 상품들의 가격의 총합과 동일해야 됩니다.");
    }

    /** 수정 **/
    @Test
    @DisplayName("주문 수정은 PENDING 상태에서만 가능해야 한다.")
    void update_whenUpdate_ShouldHavePendingStatus() {
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);
        ReflectionTestUtils.setField(order, "status", OrderStatus.SHIPPED);

        // when & then
        assertThatThrownBy(() -> order.update(orderProducts, 1L, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 수정은 PENDING 상태에서만 가능합니다.");
    }

    @Test
    @DisplayName("주문 수정시 주문 상품은 1개 이상이어야 한다")
    void update_withEmptyProductList_ShouldThrowException() {
        // given
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;
        List<OrderProduct> UpdateOrderProducts = new ArrayList<>();

        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);

        // when& then
        assertThatThrownBy(() -> order.update(UpdateOrderProducts, 1L, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 상품은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 수정시 총 가격은 주문 상품들의 가격의 총합과 동일해야된다")
    void update_whenTotalPriceMismatchesSumOfProducts_shouldThrowException(){
        // given
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;
        int updateTotalPrice = 100;

        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);

        // when & then
        assertThatThrownBy(() -> order.update(orderProducts, 1L, requestMessage, updateTotalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("총 가격은 상품들의 가격의 총합과 동일해야 됩니다.");
    }

    @Test
    @DisplayName("주문 정보 수정은 주문한 업체의 담당자만 가능하다")
    void update_byDifferentCompanyManager_ShouldThrowException() {
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);

        // when & then
        assertThatThrownBy(() -> order.update(orderProducts, 2L, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 수정은 주문한 업체 담당자만 가능합니다.");
    }

    @Test
    @DisplayName("주문 상태는 DONE/CANCELLED 경우 변경 불가능하다")
    void updateStatus() {
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);
        ReflectionTestUtils.setField(order, "status", OrderStatus.DONE); // 테스트 목적 강제 상태 변경

        //then
        assertThatThrownBy(() -> order.updateStatus(OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 상태는 DONE/CANCELLED 경우 변경 불가능합니다.");
    }

    /** 취소 **/
    @Test
    @DisplayName("주문 취소는 업체 담당자인 경우 해당 업체 담당자만 가능하다.")
    void cancel_byDifferentCompanyManager_ShouldThrowException() {
        // given
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);

        // then
        assertThatThrownBy(() -> order.cancel(UserRole.COMPANY_MANAGER, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 취소할 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문 취소는 PENDING/PAYED 상태에서만 가능하다.")
    void cancel_ShouldHavePendingOrPayedStatus() {
        // given
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);
        ReflectionTestUtils.setField(order, "status", OrderStatus.SHIPPED);

        // then
        assertThatThrownBy(() -> order.cancel(UserRole.COMPANY_MANAGER, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 취소는 PENDING/PAYED 상태에서만 가능합니다.");
    }

    @Test
    @DisplayName("주문 취소후 상태는 CANCELLED 이어야 한다")
    void cancel_afterCancel_ShouldHaveCancelledStatus() {
        List<OrderProduct> orderProducts = getOrderProduct();
        UUID companyId = UUID.randomUUID();
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = Order.create(orderProducts, 1L, companyId, requestMessage, totalPrice);
        order.cancel(UserRole.COMPANY_MANAGER, 1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }


    // 테스트 헬퍼 메서드
    private static List<OrderProduct> getOrderProduct() {
        List<OrderProduct> orderProducts = new ArrayList<>();
        UUID productId = UUID.randomUUID();
        orderProducts.add(OrderProduct.create(productId, 1000, 1, "상품1"));
        return orderProducts;
    }
}
