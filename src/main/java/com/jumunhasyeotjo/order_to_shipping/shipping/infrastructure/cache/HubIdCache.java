package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.service.RedisService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.HubInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.HubClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubIdCache {

	private static final String HUB_ID_HASH_KEY = "hub:ids";
	private static final int EXPIRE_DURATION_DAYS = 30;

	private final RedisService redisService;
	private final HubClient hubClient;

	public UUID get(String hubName) {
		return redisService.hashGetJson(
			HUB_ID_HASH_KEY,
			hubName,
			new TypeReference<UUID>() {}
		);
	}

	public void put(String hubName, UUID hubId) {
		if (hubName == null) {
			return;
		}

		redisService.hashPutJson(
			HUB_ID_HASH_KEY,
			hubName,
			hubId
		);
		redisService.expireDays(HUB_ID_HASH_KEY, EXPIRE_DURATION_DAYS);
	}

    /**
     * 캐시에 없으면 전체 허브 정보를 로딩한 뒤 다시 조회한다.
     * 두 번째 조회에도 없으면 예외 발생.
     */
    public UUID getOrLoad(String hubName) {
        UUID cached = get(hubName);
        if (cached != null) {
            return cached;
        }

        cacheAllHubInfo();

		UUID reloaded = get(hubName);
        if (reloaded == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_HUB_INFO);
        }

        return reloaded;
    }

	private void cacheAllHubInfo(){
		List<HubInfo> hubInfoList = hubClient.getAllHubs();
		hubInfoList.forEach( hubInfo -> {
			put(hubInfo.hubName(),hubInfo.hubId());
		});
	}

	public void delete(String hubName) {
		redisService.hashDelete(HUB_ID_HASH_KEY, hubName);
	}

	public void deleteAll() {
		redisService.delete(HUB_ID_HASH_KEY);
	}

}