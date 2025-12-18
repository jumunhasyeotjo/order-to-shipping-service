package com.jumunhasyeotjo.order_to_shipping.order.domain;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.OrderProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class OrderProductDomainTest {

    @Test
    @DisplayName("주문상품 정상 생성 프로세스")
    public void create_successProcess() {
        // given
        UUID productId = UUID.randomUUID();
        String name = "상품1";
        int quantity = 1;
        int price = 1000;

        // when
        OrderProduct orderProduct = OrderProduct.create(productId, price, quantity, name);

        // then
        assertThat(orderProduct.getProductId()).isEqualTo(productId);
        assertThat(orderProduct.getQuantity()).isEqualTo(quantity);
        assertThat(orderProduct.getName()).isEqualTo(name);
        assertThat(orderProduct.getPrice()).isEqualTo(price);
    }

    @Test
    @DisplayName("주문상품 가격은 1 미만이면 안된다")
    public void create_WithInvalidPrice_ShouldThrowException() {
        // given
        UUID productId = UUID.randomUUID();
        String name = "상품1";
        int quantity = 10;
        int price = 0;

        // when & then
        assertThatThrownBy(() -> OrderProduct.create(productId, price, quantity, name))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문상품의 가격은 1 미만이면 안됩니다.");
    }

    @Test
    @DisplayName("주문상품의 수량은 1 미만이면 안된다")
    public void create_WithInvalidQuantity_ShouldThrowException() {
        // given
        UUID productId = UUID.randomUUID();
        String name = "상품1";
        int quantity = 0;
        int price = 1000;

        // when & then
        assertThatThrownBy(() -> OrderProduct.create(productId, price, quantity, name))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문상품의 수량은 1 미만이면 안됩니다.");
    }
}
