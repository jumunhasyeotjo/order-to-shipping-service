package com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.config;


import com.jumunhasyeotjo.order_to_shipping.coupon.domain.repository.IssueCouponRepository;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository.IssueCouponRepositoryAdapter;
import com.jumunhasyeotjo.order_to_shipping.coupon.infrastructure.repository.JpaIssueCouponRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueCouponRepositoryConfig {
    @Bean
    public IssueCouponRepository issueCouponRepository(JpaIssueCouponRepository jpaIssueCouponRepository) {
        return new IssueCouponRepositoryAdapter(jpaIssueCouponRepository);
    }
}
