-- KEYS[1] = outbox:stream
-- KEYS[2] = idempotency prefix

-- ARGV[1] = orderId
-- ARGV[2] = orderProductsJson
-- ARGV[3] = couponId (nullable, "nil")
-- ARGV[4] = couponDiscount
-- ARGV[5] = idempotencyKey
-- ARGV[6] = eventPayloadJson

local streamKey = KEYS[1]
local idempotencyPrefix = KEYS[2]

local orderId = ARGV[1]
local orderProductsJson = ARGV[2]
local couponId = ARGV[3]
local couponDiscount = tonumber(ARGV[4] or "0")
local idempotencyKey = ARGV[5]
local eventPayload = ARGV[6]

-- 1. 멱등성 체크 (중복 요청 방지)
local idempotencyCheckKey = idempotencyPrefix .. idempotencyKey
if redis.call("EXISTS", idempotencyCheckKey) == 1 then
    return {err="DUPLICATE_REQUEST"}
end

-- 2. 상품별 재고 체크 및 차감
local orderProducts = cjson.decode(orderProductsJson)
for _, product in ipairs(orderProducts) do
    local productId = product.productId
    local quantity = tonumber(product.quantity)
    local stockKey = "bf:stock:" .. productId

    -- 재고 확인
    local currentStock = tonumber(redis.call("GET", stockKey) or "0")
    if currentStock < quantity then
        return {
            err="INSUFFICIENT_STOCK",
            productId=productId,
            required=quantity,
            available=currentStock
        }
    end

    -- 재고 차감 (예약)
    redis.call("DECRBY", stockKey, quantity)
end

-- 3. 쿠폰 차감 (있는 경우)
if couponId ~= "nil" and couponId ~= "" then
    local couponKey = "coupon:used:" .. couponId

    -- 쿠폰 사용 여부 확인
    if redis.call("EXISTS", couponKey) == 1 then
        -- 롤백: 재고 복구
        for _, product in ipairs(orderProducts) do
            redis.call("INCRBY", "bf:stock:" .. product.productId, product.quantity)
        end
        return {err="COUPON_ALREADY_USED", couponId=couponId}
    end

    -- 쿠폰 사용 처리
    redis.call("SET", couponKey, "1")
    redis.call("EXPIRE", couponKey, 86400) -- 24시간 TTL
end

-- 4. 멱등성 키 저장 (TTL 24시간)
redis.call("SETEX", idempotencyCheckKey, 86400, orderId)

-- 5. Redis Streams Outbox에 이벤트 발행
local messageId = redis.call("XADD", streamKey, "*",
    "orderId", orderId,
    "idempotencyKey", idempotencyKey,
    "payload", eventPayload,
    "status", "pending",
    "timestamp", redis.call("TIME")[1]
)

return {ok="SUCCESS", messageId=messageId, orderId=orderId}