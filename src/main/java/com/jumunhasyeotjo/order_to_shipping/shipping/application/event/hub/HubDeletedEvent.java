package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub;

import java.util.UUID;

import lombok.Getter;

public record HubDeletedEvent(
    UUID hubId,
    String name
){
}