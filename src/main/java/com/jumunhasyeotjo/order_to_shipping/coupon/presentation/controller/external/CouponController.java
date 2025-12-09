package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.external;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.CouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponOrchestratorService;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.CouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.CouponRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.ApiRes;
import com.library.passport.entity.PassportUserRole;
import com.library.passport.proto.PassportProto.Passport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
@SecurityRequirement(name = "passportHeader")
public class CouponController {

    private final CouponService couponService;
    private final IssueCouponOrchestratorService issueCouponOrchestratorService;

    @PostMapping
    @PassportAuthorize(allowedRoles = {PassportUserRole.MASTER})
    @Operation(
        summary = "쿠폰 생성",
        description = "새로운 쿠폰을 생성합니다. MASTER 권한만 호출 가능합니다."
    )
    public ResponseEntity<ApiRes<CouponRes>> createCoupon(
        @Parameter(hidden = true) @PassportUser Passport passport,
        @RequestBody CouponReq req
    ) {
        CouponRes res = CouponRes.fromResult(couponService.createCoupon(req.toCommand()));
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiRes.success(res));
    }

    @PostMapping("/init/{couponId}")
    @PassportAuthorize(allowedRoles = {PassportUserRole.MASTER})
    @Operation(
        summary = "쿠폰 재고 초기화",
        description = "쿠폰 발급을 위해 쿠폰 재고를 초기화합니다. MASTER 권한 필요."
    )
    public ResponseEntity<ApiRes<Void>> initCoupon(
        @Parameter(hidden = true) @PassportUser Passport passport,
        @PathVariable UUID couponId
    ) {
        issueCouponOrchestratorService.initStock(couponId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiRes.success(null));
    }
}