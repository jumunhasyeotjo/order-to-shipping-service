package com.jumunhasyeotjo.order_to_shipping.order.presentation;

import com.jumunhasyeotjo.order_to_shipping.common.ApiRes;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.CreateOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.OrderUpdateStatusReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CancelOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CreateOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.UpdateOrderRes;
import com.library.passport.annotation.PassportUser;
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

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiRes<CreateOrderRes>> createOrder(@RequestBody @Valid CreateOrderReq req,
                                                              @PassportUser Passport passport) {
        CreateOrderCommand command = new CreateOrderCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                req.requestMessage(),
                req.orderProducts());

        Order order = orderService.createOrder(command);
        CreateOrderRes res = new CreateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiRes.success(res));
    }

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

        Order order = orderService.updateOrderStatus(command);
        UpdateOrderRes res = new UpdateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiRes<CancelOrderRes>> deleteOrder(@PathVariable UUID orderId,
                                                              @PassportUser Passport passport) {
        CancelOrderCommand command = new CancelOrderCommand(
                passport.getUserId(),
                UUID.fromString(passport.getBelong()),
                orderId,
                passport.getRole()
        );

        Order order = orderService.cancelOrder(command);
        CancelOrderRes res = new CancelOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiRes<OrderResult>> getOrder(@PathVariable UUID orderId,
                                                        @PassportUser Passport passport) {
        GetOrderCommand command = new GetOrderCommand(
                orderId,
                UUID.fromString(passport.getBelong()),
                passport.getUserId(),
                passport.getRole()
        );

        OrderResult res = orderService.getOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

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

        Page<OrderResult> res = orderService.searchOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

}
