package com.jumunhasyeotjo.order_to_shipping.order.presentation;

import com.jumunhasyeotjo.order_to_shipping.common.ApiRes;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderService;
import com.jumunhasyeotjo.order_to_shipping.order.application.command.*;
import com.jumunhasyeotjo.order_to_shipping.order.application.dto.OrderResult;
import com.jumunhasyeotjo.order_to_shipping.order.domain.entity.Order;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.CreateOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.OrderUpdateStatusReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.request.UpdateOrderReq;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CancelOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.CreateOrderRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.UpdateOrderRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiRes<CreateOrderRes>> createOrder(@RequestBody @Valid CreateOrderReq req) {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                req.totalPrice(),
                req.requestMessage(),
                req.orderProducts());

        Order order = orderService.createOrder(command);
        CreateOrderRes res = new CreateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiRes.success(res));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiRes<UpdateOrderRes>> updateOrder(@PathVariable UUID orderId,
                                                              @RequestBody @Valid UpdateOrderReq req) {
        UpdateOrderCommand command = new UpdateOrderCommand(
                1L,
                orderId,
                req.totalPrice(),
                req.requestMessage(),
                req.orderProducts());

        Order order = orderService.updateOrder(command);
        UpdateOrderRes res = new UpdateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiRes<UpdateOrderRes>> updateOrderStatus(@PathVariable UUID orderId,
                                                                    @RequestBody @Valid OrderUpdateStatusReq req) {
        OrderUpdateStatusCommand command = new OrderUpdateStatusCommand(
                1L,
                orderId,
                "MASTER",
                req.status()
        );

        Order order = orderService.updateOrderStatus(command);
        UpdateOrderRes res = new UpdateOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiRes<CancelOrderRes>> deleteOrder(@PathVariable UUID orderId) {
        CancelOrderCommand command = new CancelOrderCommand(
                1L,
                orderId,
                "MASTER");

        Order order = orderService.cancelOrder(command);
        CancelOrderRes res = new CancelOrderRes(order.getId(), order.getStatus());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiRes<OrderResult>> getOrder(@PathVariable UUID orderId) {
        GetOrderCommand command = new GetOrderCommand(
                orderId,
                1L,
                "MASTER"
        );

        OrderResult res = orderService.getOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @GetMapping
    public ResponseEntity<ApiRes<Page<OrderResult>>> searchOrder(@PageableDefault Pageable pageable,
                                                                 @RequestParam(name = "companyId") UUID companyId) {
        SearchOrderCommand command = new SearchOrderCommand(
                1L,
                companyId,
                "MASTER",
                pageable
        );

        Page<OrderResult> res = orderService.searchOrder(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

}
