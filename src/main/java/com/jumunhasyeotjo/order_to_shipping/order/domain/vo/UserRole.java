package com.jumunhasyeotjo.order_to_shipping.order.domain.vo;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum UserRole {
    MASTER("MASTER"),
    HUB_MANAGER("HUB_MANAGER"),
    COMPANY_MANAGER("COMPANY_MANAGER");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public static UserRole convertToUserRole(String role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
