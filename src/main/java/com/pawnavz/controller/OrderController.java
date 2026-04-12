package com.pawnavz.controller;

import com.pawnavz.dto.request.CreateOrderRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Order placed successfully",
                        orderService.createOrder(uid(auth), request)));
    }

    @GetMapping
    @Operation(summary = "Get order history")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getUserOrders(uid(auth), pageable)));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @RequestHeader("Authorization") String auth,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderDetails(uid(auth), orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @RequestHeader("Authorization") String auth,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(uid(auth), orderId)));
    }
}
