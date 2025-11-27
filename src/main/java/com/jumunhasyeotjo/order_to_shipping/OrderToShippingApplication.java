package com.jumunhasyeotjo.order_to_shipping;

import com.library.passport.config.WebMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@EnableFeignClients
@ComponentScan(basePackages = {
		"com.jumunhasyeotjo.order_to_shipping",
		"com.library.passport"
})
@Import(WebMvcConfig.class)
@SpringBootApplication
public class OrderToShippingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderToShippingApplication.class, args);
	}
}
