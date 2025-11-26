package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.UserResult;

import java.util.Optional;
import java.util.UUID;

public interface OrderUserClient {
    Optional<UUID> getOrganizationId(Long userId);
    Optional<UserResult> getUser(Long userId);
}
