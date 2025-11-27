package com.jumunhasyeotjo.order_to_shipping.order.domain;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jumunhasyeotjo.order_to_shipping.order.fixtures.OrderFixtures.getOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Domain 단위 테스트
public class OrderDomainTest {

    /** 생성 **/
    @Test
    @DisplayName("주문 생성시 상태는 PENDING 이어야 한다")
    void create_ShouldHavePendingStatus() {
        // given
        String requestMessage = "요구사항";
        int totalPrice = 1000;

        // when
        Order order = getOrder();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderCompanies().size()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(order.getRequestMessage()).isEqualTo(requestMessage);
    }

    @Test
    @DisplayName("주문 생성시 주문 업체는 1개 이상이어야 한다")
    void create_withEmptyProductList_ShouldThrowException() {
        // given
        UUID companyId = UUID.randomUUID();
        List<OrderCompany> orderCompanies = new ArrayList<>();

        String requestMessage = "요구사항";
        int totalPrice = 10000;

        // when & then
        assertThatThrownBy(() -> Order.create(orderCompanies, 1L, companyId, requestMessage, totalPrice))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("잘못된 요청입니다.");
    }

    /** 수정 **/
    @Test
    @DisplayName("주문 상태는 DONE/CANCELLED 경우 변경 불가능하다")
    void updateStatus() {
        // given
        Order order = getOrder();
        ReflectionTestUtils.setField(order, "status", OrderStatus.DONE); // 테스트 목적 강제 상태 변경

        // when & then
        assertThatThrownBy(() -> order.updateStatus(OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 상태는 DONE/CANCELLED 경우 변경 불가능합니다.");
    }

    /** 취소 **/
    @Test
    @DisplayName("주문 취소는 업체 담당자인 경우 해당 업체 담당자만 가능하다.")
    void cancel_byDifferentCompanyManager_ShouldThrowException() {
        // given
        Order order = getOrder();

        // when & then
        assertThatThrownBy(() -> order.cancel(UserRole.COMPANY_MANAGER, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문을 취소할 권한이 없습니다.");
    }

    @Test
    @DisplayName("주문 취소는 PENDING/PAYED 상태에서만 가능하다.")
    void cancel_ShouldHavePendingOrPayedStatus() {
        // give
        Order order = getOrder();
        ReflectionTestUtils.setField(order, "status", OrderStatus.SHIPPED);

        // when & then
        assertThatThrownBy(() -> order.cancel(UserRole.COMPANY_MANAGER, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 취소는 PENDING/PAYED 상태에서만 가능합니다.");
    }

    @Test
    @DisplayName("주문 취소후 상태는 CANCELLED 이어야 한다")
    void cancel_afterCancel_ShouldHaveCancelledStatus() {
        //given
        Order order = getOrder();

        // when
        order.cancel(UserRole.COMPANY_MANAGER, 1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
