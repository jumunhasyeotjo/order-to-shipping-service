package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.config;


import com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository.CouponRepository;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository.CouponRepositoryAdapter;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository.JpaCouponRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponRepositoryConfig {
    @Bean
    public CouponRepository couponRepository(JpaCouponRepository jpaCouponRepository) {
        return new CouponRepositoryAdapter(jpaCouponRepository);
    }
}
