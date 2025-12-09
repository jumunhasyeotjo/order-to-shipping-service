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
        summary = "[Internal] 업체 주문 상품 조회 (ID)",
        description = "내부 시스템 간 통신용 API입니다. 특정 업체 주문에 포함된 상품 ID 목록을 반환합니다."
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
                                    "data": [
                                        { "productId": "prod-111", "quantity": 2 },
                                        { "productId": "prod-222", "quantity": 1 }
                                    ]
                                }
                                """
                        )
                )
        )
})
public @interface ApiDocInternalGetCompanyOrderItems {
}