package com.jumunhasyeotjo.order_to_shipping.payment.domain.vo;

import java.math.BigDecimal;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Money {
    
    private final Integer amount;
    
    private Money(Integer amount) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_MONEY);
        }
        if (amount < 0) {
            throw new BusinessException(ErrorCode.INVALID_MONEY);
        }
        this.amount = amount;
    }
    
    public static Money of(Integer amount) {
        return new Money(amount);
    }

    
    @Override
    public String toString() {
        return amount.toString();
    }
}
