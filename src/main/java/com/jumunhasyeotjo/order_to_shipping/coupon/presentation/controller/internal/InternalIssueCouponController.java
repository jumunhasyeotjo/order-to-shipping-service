package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.internal;


import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.UseCouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.IssueCouponRes;
import com.library.passport.entity.ApiRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/coupons/issue")
@RequiredArgsConstructor
public class InternalIssueCouponController {

    private final IssueCouponService issueCouponService;

    @PatchMapping("/use")
    @Operation(
        summary = "쿠폰 사용 처리",
        description = "발급된 쿠폰을 사용 처리합니다. Internal API."
    )
    public ResponseEntity<ApiRes<Integer>> useCoupon(
        @RequestBody UseCouponReq useCouponReq
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    issueCouponService.useCoupon(useCouponReq.toCommand())
                )
            );
    }

    @GetMapping("/{issueCouponId}")
    @Operation(
        summary = "발급 쿠폰 조회",
        description = "issueCouponId로 발급된 쿠폰 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiRes<IssueCouponRes>> getCouponById(
        @Parameter(description = "발급 쿠폰 ID")
        @PathVariable("issueCouponId") UUID issueCouponId
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    IssueCouponRes.fromResult(
                        issueCouponService.getIssueCoupon(issueCouponId)
                    )
                )
            );
    }
}
