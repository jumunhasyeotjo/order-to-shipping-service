package com.jumunhasyeotjo.order_to_shipping.order.domain;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderCompany;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderCompanyDomainTest {

    @Test
    @DisplayName("주문 업체 정상 프로세스")
    void create_SuccessProcess() {
        // given
        UUID supplierCompanyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int price = 1000;
        int quantity = 1;
        String name = "상품";

        // when
        OrderCompany orderCompany = OrderCompany.create(supplierCompanyId, List.of(new OrderProduct(productId, price, quantity, name)));

        // then
        assertThat(orderCompany).isNotNull();
        assertThat(orderCompany.getSupplierCompanyId()).isEqualTo(supplierCompanyId);
        assertThat(orderCompany.getOrderProducts().get(0).getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("주문 업체 생성시 하위의 주문 상품은 반드시 존재해야된다")
    void create_공급ID() {
        // given
        UUID supplierCompanyId = null;
        UUID productId = UUID.randomUUID();
        int price = 1000;
        int quantity = 1;
        String name = "상품";

        // when & then
        assertThatThrownBy(() -> OrderCompany.create(supplierCompanyId, List.of(new OrderProduct(productId, price, quantity, name))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("잘못된 요청입니다.");
    }

    @Test
    @DisplayName("주문 업체 생성시 하위의 주문 상품은 반드시 존재해야된다")
    void create() {
        // given
        UUID supplierCompanyId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> OrderCompany.create(supplierCompanyId, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("잘못된 요청입니다.");
    }
}
