package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public record HubCreatedEvent (
    UUID hubId,
    String name
){
}