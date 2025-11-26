package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.UserResult;
import com.jumunhasyeotjo.order_to_shipping.order.application.service.OrderUserClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderUserClientImpl implements OrderUserClient {
    @Override
    public Optional<UUID> getOrganizationId(Long userId) {
        return Optional.of(UUID.randomUUID());
    }

    @Override
    public Optional<UserResult> getUser(Long userId) {
        return Optional.of(new UserResult("테스트","test@gmail.com", UUID.randomUUID()));
    }
}
