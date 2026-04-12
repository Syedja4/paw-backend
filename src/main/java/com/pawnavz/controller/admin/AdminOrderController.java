package com.pawnavz.controller.admin;

import com.pawnavz.dto.request.AssignDriverRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.service.admin.AdminOrderService;
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
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        System.out.println("API HIT: admin endpoint");
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return ResponseEntity.ok(ApiResponse.success(
                    adminOrderService.getAllOrders(status, userId, driverId, from, to, pageable)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminOrderService.getOrderById(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<OrderResponse>> assignDriver(
            @PathVariable String id,
            @Valid @RequestBody AssignDriverRequest request) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Driver assigned successfully", adminOrderService.assignDriver(id, request)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        System.out.println("API HIT: admin endpoint");
        try {
            String status = body.get("status");
            if (status == null || status.isBlank()) throw new BadRequestException("Status is required");
            return ResponseEntity.ok(ApiResponse.success(
                    "Order status updated", adminOrderService.updateOrderStatus(id, status)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
