package com.jumunhasyeotjo.order_to_shipping.common.config;

import com.library.passport.config.WebMvcConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@ComponentScan(basePackages = {
        "com.jumunhasyeotjo.order_to_shipping",
        "com.library.passport"
})
@Import(WebMvcConfig.class)
public class PassportConfig {
}
