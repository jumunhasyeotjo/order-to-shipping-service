package com.jumunhasyeotjo.order_to_shipping.order.application.dto;

import java.util.UUID;

public record UserResult(
        String username,
        String slackId,
        UUID organizationId
) {
}
