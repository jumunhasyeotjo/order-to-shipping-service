package com.jumunhasyeotjo.order_to_shipping.common.vo;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum UserRole {
    MASTER("MASTER"),
    HUB_MANAGER("HUB_MANAGER"),
    COMPANY_MANAGER("COMPANY_MANAGER"),
    HUB_DRIVER("HUB_DRIVER"),
    COMPANY_DRIVER("COMPANY_DRIVER");

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
