package com.jumunhasyeotjo.order_to_shipping.order.presentation;

import com.jumunhasyeotjo.order_to_shipping.order.application.BFOrderOrchestrator;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderOrchestrator;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.docs.*;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.CancelOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.CreateOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.OrderUpdateStatusReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CancelOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CreateOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.UpdateOrderRes;
import com.library.passport.annotation.PassportAuthorize;
import com.library.passport.annotation.PassportUser;
import com.library.passport.entity.ApiRes;
import com.library.passport.entity.PassportUserRole;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.library.passport.proto.PassportProto.Passport;

@Tag(name = "Bf-Order", description = "주문 관리 (등록, 수정, 삭제, 조회) API")
@RestController
@RequestMapping("/api/v1/bf/orders")
@RequiredArgsConstructor
@Slf4j
public class BfOrderController {

    private final BFOrderOrchestrator orderOrchestrator;

    @ApiDocCreateOrder
    @PassportAuthorize(allowedRoles = PassportUserRole.COMPANY_MANAGER)
    @PostMapping
    public ResponseEntity<ApiRes<CreateOrderRes>> createOrder(@RequestBody @Valid CreateOrderReq req,
                                                              @RequestHeader("x-idempotency-key") String idempotencyKey
                                                             ) {
        UUID organizationId = UUID.randomUUID();
        long userId = 1L;
        log.info("organizationId : {}, userId : {]",organizationId, userId);
        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                organizationId,
                req.requestMessage(),
                req.orderProducts(),
                idempotencyKey,
                req.couponId(),
                req.tossPaymentKey(),
                req.tossOrderId());

        Order order = orderOrchestrator.createOrder(command);
        CreateOrderRes res = new CreateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiRes.success(res));
    }
}
