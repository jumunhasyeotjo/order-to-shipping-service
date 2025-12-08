package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub;

import java.util.UUID;

import lombok.Getter;

@Getter
public record HubNameUpdatedEvent(
    UUID hubId,
    String name
){
}