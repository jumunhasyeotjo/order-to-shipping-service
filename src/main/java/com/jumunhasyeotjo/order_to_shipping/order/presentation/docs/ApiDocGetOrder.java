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
        summary = "주문 단건 조회",
        description = "특정 주문의 상세 정보를 조회합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
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
                                        "companyId": "220e8400-e29b-41d4-a716-446655440002",
                                        "status": "ORDERED",
                                        "totalPrice": 30000,
                                        "requestMessage": "부재시 문앞",
                                        "orderProducts": [
                                            { "productId": "p-1", "name": "사과", "quantity": 2, "price": 10000 },
                                            { "productId": "p-2", "name": "배", "quantity": 1, "price": 10000 }
                                        ]
                                    }
                                }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "조회 권한 없음",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "FORBIDDEN_GET_ORDER", "message": "해당 주문을 조회할 권한이 없습니다.", "data": null }
                        """))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "주문 없음",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "ORDER_NOT_FOUND", "message": "주문을 찾을 수 없습니다.", "data": null }
                        """))
        )
})
public @interface ApiDocGetOrder {
}