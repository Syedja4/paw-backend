package com.pawnavz.controller.shop;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ShopOrderResponse;
import com.pawnavz.service.ShopOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops/orders")
@PreAuthorize("hasRole('SHOP')")
@RequiredArgsConstructor
@Tag(name = "Shop Orders")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;

    @GetMapping
    @Operation(summary = "List orders routed to the shop")
    public ResponseEntity<ApiResponse<Page<ShopOrderResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(shopOrderService.getMyOrders(status, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shop order by ID")
    public ResponseEntity<ApiResponse<ShopOrderResponse>> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shopOrderService.getOrderById(id)));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accept an order")
    public ResponseEntity<ApiResponse<ShopOrderResponse>> accept(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Order accepted", shopOrderService.acceptOrder(id)));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject an order")
    public ResponseEntity<ApiResponse<ShopOrderResponse>> reject(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Order rejected", shopOrderService.rejectOrder(id)));
    }

    @PatchMapping("/{id}/ready")
    @Operation(summary = "Mark an order as ready for pickup")
    public ResponseEntity<ApiResponse<ShopOrderResponse>> ready(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Order marked ready", shopOrderService.markReady(id)));
    }
}
