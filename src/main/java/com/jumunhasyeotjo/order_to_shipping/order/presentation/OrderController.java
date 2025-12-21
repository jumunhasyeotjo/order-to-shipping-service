package com.jumunhasyeotjo.order_to_shipping.order.presentation;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.library.passport.proto.PassportProto.Passport;

@Tag(name = "Order", description = "주문 관리 (등록, 수정, 삭제, 조회) API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderOrchestrator orderOrchestrator;

    @ApiDocCreateOrder
    @PassportAuthorize(allowedRoles = PassportUserRole.COMPANY_MANAGER)
    @PostMapping
    public ResponseEntity<ApiRes<CreateOrderRes>> createOrder(@RequestBody @Valid CreateOrderReq req,
                                                              @RequestHeader("x-idempotency-key") String idempotencyKey,
                                                              @PassportUser Passport passport) {
        CreateOrderCommand command = new CreateOrderCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                req.requestMessage(),
                req.orderProducts(),
                idempotencyKey,
                req.couponId());
//                req.tossPaymentKey(),
//                req.tossOrderId());

        Order order = orderOrchestrator.createOrder(command);
        CreateOrderRes res = new CreateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiRes.success(res));
    }

    @ApiDocUpdateOrderStatus
    @PassportAuthorize(allowedRoles = {PassportUserRole.HUB_MANAGER, PassportUserRole.MASTER})
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiRes<UpdateOrderRes>> updateOrderStatus(@PathVariable UUID orderId,
                                                                    @RequestBody @Valid OrderUpdateStatusReq req,
                                                                    @PassportUser Passport passport) {
        OrderUpdateStatusCommand command = new OrderUpdateStatusCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                orderId,
                passport.getRole(),
                req.status()
        );

        Order order = orderOrchestrator.updateOrderStatus(command);
        UpdateOrderRes res = new UpdateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @ApiDocCancelOrder
    @PassportAuthorize(allowedRoles = {PassportUserRole.COMPANY_MANAGER, PassportUserRole.MASTER, PassportUserRole.HUB_MANAGER})
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiRes<CancelOrderRes>> deleteOrder(@PathVariable UUID orderId,
                                                              @RequestBody CancelOrderReq req,
                                                              @PassportUser Passport passport)
    {
        CancelOrderCommand command = new CancelOrderCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                orderId,
                passport.getRole(),
                req.reason()
        );

        Order order = orderOrchestrator.cancelOrder(command);
        CancelOrderRes res = new CancelOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @ApiDocGetOrder
    @PassportAuthorize(allowedRoles = {PassportUserRole.COMPANY_MANAGER, PassportUserRole.MASTER, PassportUserRole.HUB_MANAGER})
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiRes<OrderResult>> getOrder(@PathVariable UUID orderId,
                                                        @PassportUser Passport passport) {
        GetOrderCommand command = new GetOrderCommand(
                orderId,
                UUID.fromString(passport.getBelong()),
                passport.getUserId(),
                passport.getRole()
        );

        OrderResult res = orderOrchestrator.getOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @ApiDocSearchOrder
    @PassportAuthorize(allowedRoles = {PassportUserRole.COMPANY_MANAGER, PassportUserRole.MASTER, PassportUserRole.HUB_MANAGER})
    @GetMapping
    public ResponseEntity<ApiRes<Page<OrderResult>>> searchOrder(@PageableDefault Pageable pageable,
                                                                 @RequestParam(name = "companyId") UUID companyId,
                                                                 @PassportUser Passport passport
    ) {
        SearchOrderCommand command = new SearchOrderCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                companyId,
                passport.getRole(),
                pageable
        );

        Page<OrderResult> res = orderOrchestrator.searchOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }
}
