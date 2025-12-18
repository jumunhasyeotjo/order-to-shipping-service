package com.jumunhasyeotjo.order_to_shipping.order.application.command;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record SearchOrderCommand(
        Long userId,
        UUID organizationId,
        UUID companyId,
        String role,
        Pageable pageable
) {
}
