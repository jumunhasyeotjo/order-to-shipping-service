package com.jumunhasyeotjo.order_to_shipping.order.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class RedisOutboxPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private DefaultRedisScript<Map> luaScript;

    public RedisOutboxPublisher(
            @Qualifier("BfredisTemplate") RedisTemplate<String, Object> bfRedisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = bfRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // Lua Script 로드
        luaScript = new DefaultRedisScript<>();
        luaScript.setLocation(new ClassPathResource("lua/order-outbox.lua"));
        luaScript.setResultType(Map.class);
    }

    /**
     * Lua Script를 사용하여 원자적으로 이벤트 발행
     *
     * @param orderId 주문 ID
     * @param idempotencyKey 멱등키
     * @param productList 상품 목록
     * @param couponId 쿠폰 ID (nullable)
     * @param eventPayload 이벤트 페이로드 (OutboxEventPayload)
     * @return Redis Streams MessageId
     */
    public String publishWithLuaScript(
            UUID orderId,
            String idempotencyKey,
            List<Map<String, Object>> productList,
            UUID couponId,
            OutboxEventPayload eventPayload) {

        try {
            log.info("[Lua Script 실행 시작] orderId: {}, idempotencyKey: {}", orderId, idempotencyKey);

            // 1. KEYS 준비
            List<String> keys = prepareKeys();

            // 2. ARGV 준비
            List<String> argv = prepareArgv(orderId, idempotencyKey, productList, couponId, eventPayload);

            // 3. Lua Script 실행
            Map<String, Object> result = redisTemplate.execute(
                    luaScript,
                    keys,
                    argv.toArray()
            );

            // 4. 결과 처리
            return handleLuaScriptResult(result, orderId);

        } catch (Exception e) {
            log.error("[Lua Script 실행 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OUTBOX_PUBLISH_FAILED);
        }
    }

    /**
     * KEYS 배열 준비
     *
     * KEYS[1] = outbox:stream (Redis Streams key)
     * KEYS[2] = idempotency:{idempotencyKey} (멱등성 체크용)
     */
    private List<String> prepareKeys() {
        return Arrays.asList(
                "outbox:order-created",           // KEYS[1]: Redis Streams
                "idempotency:"                    // KEYS[2]: 멱등성 키 prefix (Lua에서 조합)
        );
    }

    /**
     * ARGV 배열 준비
     *
     * ARGV[1] = orderId
     * ARGV[2] = orderProductsJson (상품 목록 JSON)
     * ARGV[3] = couponId (nullable, "nil"로 표시)
     * ARGV[4] = couponDiscount (0 if no coupon)
     * ARGV[5] = idempotencyKey
     * ARGV[6] = eventPayload (전체 이벤트 JSON)
     */
    private List<String> prepareArgv(
            UUID orderId,
            String idempotencyKey,
            List<Map<String, Object>> productList,
            UUID couponId,
            OutboxEventPayload eventPayload) {

        try {
            List<String> argv = new ArrayList<>();

            // ARGV[1]: orderId
            argv.add(orderId.toString());

            // ARGV[2]: orderProductsJson
            String orderProductsJson = objectMapper.writeValueAsString(productList);
            argv.add(orderProductsJson);

            // ARGV[3]: couponId (nullable)
            argv.add(couponId != null ? couponId.toString() : "nil");

            // ARGV[4]: couponDiscount (현재는 0, 실제로는 쿠폰 할인액 계산 필요)
            argv.add("0");

            // ARGV[5]: idempotencyKey
            argv.add(idempotencyKey);

            // ARGV[6]: eventPayload (전체 이벤트 JSON)
            String eventPayloadJson = objectMapper.writeValueAsString(eventPayload);
            argv.add(eventPayloadJson);

            return argv;

        } catch (JsonProcessingException e) {
            log.error("[JSON 변환 실패] orderId: {}", orderId, e);
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    /**
     * Lua Script 실행 결과 처리
     */
    private String handleLuaScriptResult(Map<String, Object> result, UUID orderId) {
        if (result == null) {
            throw new BusinessException(ErrorCode.LUA_SCRIPT_EXECUTION_FAILED);
        }

        // 에러 체크
        if (result.containsKey("err")) {
            String errorCode = (String) result.get("err");
            log.error("[Lua Script 에러] orderId: {}, errorCode: {}", orderId, errorCode);

            throw switch (errorCode) {
                case "DUPLICATE_REQUEST" -> new BusinessException(ErrorCode.DUPLICATE_ORDER);
                case "INSUFFICIENT_STOCK" -> {
                    log.info(String.format("productId: %s, required: %s, available: %s",
                            result.get("productId"),
                            result.get("required"),
                            result.get("available")));
                    throw new BusinessException(
                        ErrorCode.INVALID_PRODUCT_STOCK
                );

                }
                case "COUPON_ALREADY_USED" -> {
                    log.error("couponId: " + result.get("couponId"));
                    throw new BusinessException(ErrorCode.COUPON_VALIDATION_FAILED);
                }
                default -> new BusinessException(ErrorCode.LUA_SCRIPT_EXECUTION_FAILED);
            };
        }

        // 성공 처리
        if ("SUCCESS".equals(result.get("ok"))) {
            String messageId = (String) result.get("messageId");
            log.info("[Lua Script 실행 성공] orderId: {}, messageId: {}", orderId, messageId);
            return messageId;
        }

        throw new BusinessException(ErrorCode.LUA_SCRIPT_EXECUTION_FAILED);
    }

    /**
     * 보상 트랜잭션 이벤트 발행
     */
    public void publishCompensationEvent(UUID orderId, String reason) {
        try {
            Map<String, Object> compensationEvent = Map.of(
                    "type", "COMPENSATION",
                    "orderId", orderId.toString(),
                    "reason", reason,
                    "timestamp", System.currentTimeMillis()
            );

            redisTemplate.opsForStream().add(
                    "outbox:compensation",
                    compensationEvent
            );

            log.info("[보상 트랜잭션 발행] orderId: {}, reason: {}", orderId, reason);

        } catch (Exception e) {
            log.error("[보상 트랜잭션 발행 실패] orderId: {}, error: {}", orderId, e.getMessage(), e);
        }
    }
}