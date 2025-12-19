package com.jumunhasyeotjo.order_to_shipping.order.presentation;

import com.jumunhasyeotjo.order_to_shipping.order.presentation.docs.ApiDocInternalGetCompanyOrderItems;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.docs.ApiDocInternalGetCompanyOrderItemsName;
import com.library.passport.entity.ApiRes;
import com.jumunhasyeotjo.order_to_shipping.order.application.OrderOrchestrator;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsNameRes;
import com.jumunhasyeotjo.order_to_shipping.order.presentation.dto.response.VendorOrderItemsRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Internal-Order", description = "내부 서비스 간 통신용 API")
@RestController
@RequestMapping("/internal/api/v1/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderOrchestrator orderOrchestrator;

    @ApiDocInternalGetCompanyOrderItems
    @GetMapping("/order-products")
    public ResponseEntity<ApiRes<List<VendorOrderItemsRes>>> getCompanyOrderItems(@RequestParam("companyOrderId") UUID companyOrderId) {
        List<VendorOrderItemsRes> res = orderOrchestrator.getCompanyOrderItems(companyOrderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }

    @ApiDocInternalGetCompanyOrderItemsName
    @GetMapping("/order-products-name")
    public ResponseEntity<ApiRes<List<VendorOrderItemsNameRes>>> getCompanyOrderItemsName(@RequestParam("companyOrderId") UUID companyOrderId) {
        List<VendorOrderItemsNameRes> res = orderOrchestrator.getCompanyOrderItemsName(companyOrderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiRes.success(res));
    }
}
