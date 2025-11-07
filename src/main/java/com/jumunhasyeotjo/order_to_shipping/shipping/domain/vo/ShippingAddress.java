package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배송 주소 값 객체
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {
    
    private String address;
    
    private ShippingAddress(String address) {
        validateAddress(address);
        this.address = address;
    }
    
    public static ShippingAddress of(String address) {
        return new ShippingAddress(address);
    }
    
    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new BusinessException(REQUIRED_VALUE_MISSING);
        }
    }
}

