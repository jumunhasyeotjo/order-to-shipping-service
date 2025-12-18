package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub;

import java.util.UUID;


public record HubCreatedEvent (
    UUID hubId,
    String name
){
}