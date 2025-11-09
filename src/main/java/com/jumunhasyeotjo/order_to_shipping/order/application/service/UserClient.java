package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import java.util.Optional;
import java.util.UUID;

public interface UserClient {
    Optional<UUID> getHubId(Long userId);
    Optional<UUID> getCompanyId(Long userId);
}
