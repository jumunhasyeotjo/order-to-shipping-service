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
public class HubNameCache {

	private static final String HUB_NAME_HASH_KEY = "hub:names";
	private static final int EXPIRE_DURATION_DAYS = 30;

	private final RedisService redisService;
	private final HubClient hubClient;

	public String get(UUID hubId) {
		return redisService.hashGetJson(
			HUB_NAME_HASH_KEY,
			field(hubId),
			new TypeReference<String>() {}
		);
	}

	public void put(UUID hubId, String hubName) {
		if (hubName == null) {
			return;
		}

		redisService.hashPutJson(
			HUB_NAME_HASH_KEY,
			field(hubId),
			hubName
		);
		redisService.expireDays(HUB_NAME_HASH_KEY, EXPIRE_DURATION_DAYS);
	}

    /**
     * 캐시에 없으면 전체 허브 정보를 로딩한 뒤 다시 조회한다.
     * 두 번째 조회에도 없으면 예외 발생.
     */
    public String getOrLoad(UUID hubId) {
        String cached = get(hubId);
        if (cached != null) {
            return cached;
        }

        cacheAllHubInfo();

        String reloaded = get(hubId);
        if (reloaded == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_HUB_INFO);
        }

        return reloaded;
    }

	public void cacheAllHubInfo(){
		List<HubInfo> hubInfoList = hubClient.getAllHubs();
		hubInfoList.forEach( hubInfo -> {
			put(hubInfo.hubId(), hubInfo.hubName());
		});
	}

	public void delete(UUID hubId) {
		redisService.hashDelete(HUB_NAME_HASH_KEY, field(hubId));
	}

	public void deleteAll() {
		redisService.delete(HUB_NAME_HASH_KEY);
	}

	private String field(UUID hubId) {
		return hubId.toString();
	}
}