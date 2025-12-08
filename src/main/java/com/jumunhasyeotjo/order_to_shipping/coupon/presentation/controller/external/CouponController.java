package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.external;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.CouponService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponOrchestratorService;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.CouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.CouponRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.ApiRes;
import com.library.passport.entity.PassportUserRole;
import com.library.passport.proto.PassportProto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final IssueCouponOrchestratorService issueCouponOrchestratorService;

    @PostMapping
    @PassportAuthorize(
        allowedRoles = {PassportUserRole.MASTER}
    )
    public ResponseEntity<ApiRes<CouponRes>> createCoupon(
        @PassportUser PassportProto.Passport passport,
        @RequestBody CouponReq req
    ) {
        CouponRes res = CouponRes.fromResult(couponService.createCoupon(req.toCommand()));
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiRes.success(res));
    }

    @PostMapping("/init/{couponId}")
    @PassportAuthorize(
        allowedRoles = {PassportUserRole.MASTER}
    )
    public ResponseEntity<ApiRes<Void>> initCoupon(
        @PassportUser PassportProto.Passport passport,
        @PathVariable("couponId") UUID couponId
    ) {
        issueCouponOrchestratorService.initStock(couponId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiRes.success(null));
    }
}
