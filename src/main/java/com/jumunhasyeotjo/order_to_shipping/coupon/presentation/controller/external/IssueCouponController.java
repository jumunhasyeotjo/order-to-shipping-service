package com.jumunhasyeotjo.order_to_shipping.coupon.presentation.controller.external;

import com.jumunhasyeotjo.order_to_shipping.coupon.application.IssueCouponOrchestratorService;
import com.jumunhasyeotjo.order_to_shipping.coupon.application.command.IssueCouponCommand;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.req.IssueCouponReq;
import com.jumunhasyeotjo.order_to_shipping.coupon.presentation.dto.res.IssueCouponRes;
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
@RequestMapping("/api/v1/coupons/issue")
@RequiredArgsConstructor
@SecurityRequirement(name = "passportHeader")
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
    @Operation(
        summary = "쿠폰 발급 요청 (Kafka 비동기)",
        description = "Kafka를 통해 비동기로 쿠폰 발급 요청을 전송합니다."
    )
    public ResponseEntity<ApiRes<Void>> issueCouponWithKafka(
        @Parameter(hidden = true) @PassportUser Passport passport,
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
    @Operation(
        summary = "쿠폰 발급 큐 등록",
        description = "쿠폰 발급을 위해 사용자에게 대기열 번호(queue)를 발급합니다."
    )
    public ResponseEntity<ApiRes<Long>> joinQueue(
        @Parameter(hidden = true) @PassportUser Passport passport,
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
    @Operation(
        summary = "발급 대기열 순번 조회",
        description = "해당 사용자(userId)의 현재 대기열 순번을 조회합니다."
    )
    public ResponseEntity<ApiRes<Long>> getUserPosition(
        @Parameter(hidden = true) @PassportUser Passport passport,
        @RequestParam UUID couponId,
        @RequestParam Long userId
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiRes.success(
                issueCouponOrchestratorService.getUserPosition(
                    new IssueCouponCommand(couponId, userId)
                )
            ));
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
    @Operation(
        summary = "쿠폰 최종 발급",
        description = "대기열에서 순서가 도래한 사용자에게 실제 쿠폰을 발급합니다."
    )
    public ResponseEntity<ApiRes<IssueCouponRes>> issueCoupon(
        @Parameter(hidden = true) @PassportUser Passport passport,
        @RequestBody IssueCouponReq req
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiRes.success(
                IssueCouponRes.fromResult(
                    issueCouponOrchestratorService.issueCoupon(req.toCommand())
                )
            ));
    }
}

