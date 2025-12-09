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
        summary = "주문 목록 검색",
        description = "조건(CompanyId 등)에 맞는 주문 목록을 페이징하여 조회합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공 (Page)",
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
                                        "content": [
                                            { "orderId": "110e...", "status": "ORDERED", "totalPrice": 15000 },
                                            { "orderId": "111e...", "status": "SHIPPING", "totalPrice": 20000 }
                                        ],
                                        "pageable": {
                                            "pageNumber": 0,
                                            "pageSize": 10,
                                            "sort": { "empty": true, "sorted": false, "unsorted": true },
                                            "offset": 0,
                                            "paged": true,
                                            "unpaged": false
                                        },
                                        "last": true,
                                        "totalElements": 2,
                                        "totalPages": 1,
                                        "size": 10,
                                        "number": 0,
                                        "sort": { "empty": true, "sorted": false, "unsorted": true },
                                        "first": true,
                                        "numberOfElements": 2,
                                        "empty": false
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
        )
})
public @interface ApiDocSearchOrder {
}