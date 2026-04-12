package com.pawnavz.controller.admin;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.UserResponse;
import com.pawnavz.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        System.out.println("API HIT: admin endpoint");
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return ResponseEntity.ok(ApiResponse.success(adminUserService.getAllUsers(search, role, blocked, pageable)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminUserService.getUserById(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<UserResponse>> blockUser(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success("User blocked successfully", adminUserService.blockUser(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblockUser(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", adminUserService.unblockUser(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
