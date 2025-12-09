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
        summary = "주문 상태 변경",
        description = "허브 관리자(HUB_MANAGER) 또는 마스터(MASTER)가 주문의 상태를 변경합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상태 변경 성공",
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
                                        "status": "SHIPPING"
                                    }
                                }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "권한 없음 (허브 관리자 아님)",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "FORBIDDEN_ORDER_HUB", "message": "해당 지역 허브 관리자 권한이 필요합니다.", "data": null }
                        """))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "주문 없음",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "ORDER_NOT_FOUND", "message": "주문을 찾을 수 없습니다.", "data": null }
                        """))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "상태 변경 불가",
                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        { "code": "ORDER_STATUS_UNCHANGEABLE", "message": "변경할 수 없는 주문 상태입니다.", "data": null }
                        """))
        )
})
public @interface ApiDocUpdateOrderStatus {
}