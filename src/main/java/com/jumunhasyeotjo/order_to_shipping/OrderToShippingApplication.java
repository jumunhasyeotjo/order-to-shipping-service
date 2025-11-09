package com.jumunhasyeotjo.order_to_shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderToShippingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderToShippingApplication.class, args);
	}

}
