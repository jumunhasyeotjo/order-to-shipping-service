package com.jumunhasyeotjo.order_to_shipping.common.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.ShortestPathPreBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 앱 기동시 최단경로 캐시 선계산
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RoutePrecomputeStartupConfig {

    private final ShortestPathPreBuilder preBuilder;

    @Bean
    @Profile("!test")
    public ApplicationRunner precomputeRoutesOnStartup() {
        log.info("최단경로 캐시 선계산");
        return args -> preBuilder.rebuildAllPairsForBoth();
    }
}