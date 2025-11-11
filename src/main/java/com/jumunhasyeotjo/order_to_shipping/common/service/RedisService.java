package com.jumunhasyeotjo.order_to_shipping.common.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;


    /**
     * 키 삭제
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Hash에서 JSON을 읽어서 객체로 역직렬화
     */
    public <T> T hashGetJson(String key, String field, TypeReference<T> typeRef) {
        String json = (String) stringRedisTemplate.opsForHash().get(key, field);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hash에 객체를 JSON으로 직렬화하여 저장
     */
    public void hashPutJson(String key, String field, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForHash().put(key, field, json);
        } catch (Exception ignore) {}
    }

    /**
     * 키 만료(일 단위)
     */
    public void expireDays(String key, long days) {
        stringRedisTemplate.expire(key, Duration.ofDays(days));
    }

}