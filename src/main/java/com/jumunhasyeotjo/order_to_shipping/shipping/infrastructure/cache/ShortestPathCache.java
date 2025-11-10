package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import static com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.RouteCacheKeys.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShortestPathCache {
  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  private static final Integer EXPIRE_DURATION_DAYS = 30;

  public Map<String,Object> get(WeightStrategy s, UUID o, UUID d){
    String key = redisHashKey(s);
    String field = field(o,d);

    String json = (String) redis.opsForHash().get(key, field);
    if (json == null) return null;
    try { return objectMapper.readValue(json, new TypeReference<>(){}); }
    catch(Exception e){ return null; }
  }

  public void put(WeightStrategy s, UUID o, UUID d, Map<String,?> dto){
    String key = redisHashKey(s);
    String field = field(o,d);

    try {
      String json = objectMapper.writeValueAsString(dto);
      redis.opsForHash().put(key, field, json);
      redis.expire(key, Duration.ofDays(EXPIRE_DURATION_DAYS));
    } catch(Exception ignore){}
  }

  public void deleteAll(){ 
    redis.delete(redisHashKey(WeightStrategy.DISTANCE));
    redis.delete(redisHashKey(WeightStrategy.DURATION));
  }
}