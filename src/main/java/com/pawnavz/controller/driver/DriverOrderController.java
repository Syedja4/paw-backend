package com.pawnavz.controller.driver;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.service.driver.DriverOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/driver/orders")
@PreAuthorize("hasRole('DRIVER')")
@RequiredArgsConstructor
public class DriverOrderController {

    private final DriverOrderService driverOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(driverOrderService.getMyOrders(status, pageable)));
    }

    @PatchMapping("/{id}/pickup")
    public ResponseEntity<ApiResponse<OrderResponse>> pickupOrder(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order picked up successfully", driverOrderService.markPickedUp(id)));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order delivered successfully", driverOrderService.markDelivered(id)));
    }
}
