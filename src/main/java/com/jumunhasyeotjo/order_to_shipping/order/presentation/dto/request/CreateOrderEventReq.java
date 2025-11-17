package com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request;

import java.util.UUID;

public record CreateOrderEventReq(

        UUID productId,
        int quantity,
        UUID ReceiverId
) {

}
