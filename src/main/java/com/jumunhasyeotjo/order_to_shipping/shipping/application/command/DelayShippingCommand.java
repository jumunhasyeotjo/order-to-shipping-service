package com.jumunhasyeotjo.order_to_shipping.shipping.application.command;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.DelayReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record DelayShippingCommand(
        Long driverId,
        UUID shippingId,
        DelayReason reason,
        String location,
        LocalDateTime delayTime,
        String etc

) {

}
