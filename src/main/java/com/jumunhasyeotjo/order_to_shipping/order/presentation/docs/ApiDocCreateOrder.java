package com.jumunhasyeotjo.order_to_shipping.order.presentation.docs;

import com.library.passport.entity.ApiRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "주문 생성",
        description = "업체 관리자(COMPANY_MANAGER)가 상품 주문을 생성합니다. 멱등성 키(x-idempotency-key) 헤더가 필요합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "주문 생성 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiRes.class),
                        examples = @ExampleObject(
                                name = "성공 예시",
                                value = """
                                {
                                    "code": null,
                                    "message": null,
                                    "data": {
                                        "orderId": "110e8400-e29b-41d4-a716-446655440001",
                                        "status": "ORDERED"
                                    }
                                }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "재고 부족 또는 결제 실패",
                content = @Content(mediaType = "application/json", examples = {
                        @ExampleObject(name = "재고 부족", value = """
                                { "code": "INVALID_PRODUCT_STOCK", "message": "상품의 재고가 부족합니다.", "data": null }
                                """),
                        @ExampleObject(name = "입력값 오류", value = """
                                { "code": "INVALID_INPUT", "message": "입력값이 유효하지 않습니다.", "data": null }
                                """)
                })
        ),
        @ApiResponse(
                responseCode = "404",
                description = "상품 또는 업체 없음",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "PRODUCT_NOT_FOUND", "message": "상품을 찾을 수 없습니다.", "data": null }
                        """))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "중복된 주문 (멱등성 위배)",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "DUPLICATE_ORDER", "message": "이미 처리된 주문입니다.", "data": null }
                        """))
        )
})
public @interface ApiDocCreateOrder {
}