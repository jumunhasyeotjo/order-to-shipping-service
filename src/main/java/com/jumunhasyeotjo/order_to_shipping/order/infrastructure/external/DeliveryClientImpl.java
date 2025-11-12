package com.jumunhasyeotjo.order_to_shipping.order.infrastructure.external;

import com.jumunhasyeotjo.order_to_shipping.order.application.service.DeliveryClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "delivery-service")
public interface DeliveryClientImpl extends DeliveryClient {
}
