package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber {

    private String value;

    private static final String PHONE_REGEX = "^(\\+\\d{1,3})?\\d{7,11}$";

    private PhoneNumber(String value) {
        validate(value);
        this.value = normalize(value);
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(REQUIRED_VALUE_MISSING);
        }

        String normalized = normalize(value);

        if (!normalized.matches(PHONE_REGEX)) {
            throw new BusinessException(INVALID_FORMAT);
        }
    }

    /**
     * 입력 값 정규화
     * ex) "010-1234-5678" → "01012345678"
     */
    private String normalize(String value) {
        return value.replaceAll("[^0-9+]", "");
    }
}