package com.pawnavz.controller.admin;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminStatsService.getOverview()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueStats() {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminStatsService.getRevenueStats()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStats() {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminStatsService.getOrderStats()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
