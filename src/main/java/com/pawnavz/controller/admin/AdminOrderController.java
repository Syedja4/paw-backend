package com.pawnavz.controller.admin;

import com.pawnavz.dto.request.AssignShopRequest;
import com.pawnavz.dto.request.RequestDeliveryRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.service.admin.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(summary = "List all orders with optional filters")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(
                adminOrderService.getAllOrders(status, userId, shopId, from, to, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminOrderService.getOrderById(id)));
    }

    @PatchMapping("/{id}/assign-shop")
    @Operation(summary = "Route an order to a shop for fulfillment")
    public ResponseEntity<ApiResponse<OrderResponse>> assignShop(
            @PathVariable String id,
            @Valid @RequestBody AssignShopRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order routed to shop", adminOrderService.assignShop(id, request)));
    }

    @PostMapping("/{id}/request-delivery")
    @Operation(summary = "Request a third-party delivery (Porter/Ola integration point)")
    public ResponseEntity<ApiResponse<OrderResponse>> requestDelivery(
            @PathVariable String id,
            @RequestBody(required = false) RequestDeliveryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Delivery requested", adminOrderService.requestDelivery(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) throw new BadRequestException("Status is required");
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated", adminOrderService.updateOrderStatus(id, status)));
    }
}
