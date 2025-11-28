package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer implements ApplicationRunner {

	private final HubNameCache hubNameCache;
	private final ShortestPathPreBuilder shortestPathPreBuilder;

	@Override
	public void run(ApplicationArguments args) {
		log.info("===== 애플리케이션 최초 구동: 캐시 Preload 시작 =====");

		try {
			hubNameCache.cacheAllHubInfo();
			log.info("허브명 캐싱 완료");

			shortestPathPreBuilder.rebuildAllPairs(WeightStrategy.DISTANCE);
			log.info("허브 경로 캐싱 완료");

			log.info("===== 캐시 Preload 완료 =====");

		} catch (Exception e) {
			log.error("초기 캐싱 중 오류 발생", e);
		}
	}
}