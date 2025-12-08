package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.external;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponOrchestratorService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.IssueCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.IssueCouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.IssueCouponRes;
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
@RequestMapping("/v1/coupons/issue")
@RequiredArgsConstructor
public class IssueCouponController {
    private final IssueCouponOrchestratorService issueCouponOrchestratorService;

    @PostMapping("/async")
    @PassportAuthorize(
        allowedRoles = {
            PassportUserRole.COMPANY_MANAGER,
            PassportUserRole.COMPANY_DRIVER,
            PassportUserRole.HUB_MANAGER,
            PassportUserRole.HUB_DRIVER,
            PassportUserRole.MASTER
        }
    )
    public ResponseEntity<ApiRes<Void>> issueCouponWithKafka(
        @PassportUser PassportProto.Passport passport,
        @RequestBody IssueCouponReq req
    ) {
        issueCouponOrchestratorService.issueCouponWithKafka(req.toCommand());
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiRes.success(null));
    }

    @PostMapping("/enqueue")
    @PassportAuthorize(
        allowedRoles = {
            PassportUserRole.COMPANY_MANAGER,
            PassportUserRole.COMPANY_DRIVER,
            PassportUserRole.HUB_MANAGER,
            PassportUserRole.HUB_DRIVER,
            PassportUserRole.MASTER
        }
    )
    public ResponseEntity<ApiRes<Long>> joinQueue(
        @PassportUser PassportProto.Passport passport,
        @RequestBody IssueCouponReq req
    ) {
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiRes.success(
                issueCouponOrchestratorService.joinQueue(req.toCommand())
            ));
    }

    @GetMapping("/position")
    @PassportAuthorize(
        allowedRoles = {
            PassportUserRole.COMPANY_MANAGER,
            PassportUserRole.COMPANY_DRIVER,
            PassportUserRole.HUB_MANAGER,
            PassportUserRole.HUB_DRIVER,
            PassportUserRole.MASTER
        }
    )
    public ResponseEntity<ApiRes<Long>> getUserPosition(
        @PassportUser PassportProto.Passport passport,
        @RequestParam("couponId") UUID couponId,
        @RequestParam("userId") Long userId
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    issueCouponOrchestratorService.getUserPosition(new IssueCouponCommand(couponId, userId))
                )
            );
    }

    @PostMapping("/confirm")
    @PassportAuthorize(
        allowedRoles = {
            PassportUserRole.COMPANY_MANAGER,
            PassportUserRole.COMPANY_DRIVER,
            PassportUserRole.HUB_MANAGER,
            PassportUserRole.HUB_DRIVER,
            PassportUserRole.MASTER
        }
    )
    public ResponseEntity<ApiRes<IssueCouponRes>> issueCoupon(
        @PassportUser PassportProto.Passport passport,
        @RequestBody IssueCouponReq req
    ) {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiRes.success(
                    IssueCouponRes.fromResult(issueCouponOrchestratorService.issueCoupon(req.toCommand()))
                )
            );
    }
}
