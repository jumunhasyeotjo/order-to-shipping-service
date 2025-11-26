package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import static com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.RouteCacheKeys.*;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jumunhasyeotjo.order_to_shipping.common.service.RedisService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShortestPathCache {
  private final RedisService redisService;

  private static final Integer EXPIRE_DURATION_DAYS = 30;

  public Map<String,Object> get(WeightStrategy s, UUID o, UUID d){
    String key = redisHashKey(s);
    String field = field(o, d);
    return redisService.hashGetJson(key, field, new TypeReference<Map<String,Object>>(){});
  }

  public void put(WeightStrategy s, UUID o, UUID d, Map<String,?> dto){
    String key = redisHashKey(s);
    String field = field(o, d);
    redisService.hashPutJson(key, field, dto);
    redisService.expireDays(key, EXPIRE_DURATION_DAYS);
  }

  public void deleteAll(){
    redisService.delete(redisHashKey(WeightStrategy.DISTANCE));
    redisService.delete(redisHashKey(WeightStrategy.DURATION));
  }
}