package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.internal;


import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.UseCouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.IssueCouponRes;
import com.library.passport.entity.ApiRes;
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
    public ResponseEntity<ApiRes<IssueCouponRes>> useCoupon(@RequestBody UseCouponReq useCouponReq) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    IssueCouponRes.fromResult(issueCouponService.useCoupon(useCouponReq.toCommand()))
                )
            );
    }

    @GetMapping("/{issueCouponId}")
    public ResponseEntity<ApiRes<IssueCouponRes>> getCouponById(@PathVariable("issueCouponId") UUID issueCouponId) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    IssueCouponRes.fromResult(issueCouponService.getIssueCoupon(issueCouponId))
                )
            );
    }
}
