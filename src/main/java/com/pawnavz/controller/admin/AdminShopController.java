package com.pawnavz.controller.admin;

import com.pawnavz.dto.request.AdminCreateShopRequest;
import com.pawnavz.dto.request.UpdateShopRequest;
import com.pawnavz.dto.request.UpdateShopStatusRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ShopResponse;
import com.pawnavz.service.admin.AdminShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/shops")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Shops")
public class AdminShopController {

    private final AdminShopService adminShopService;

    @PostMapping
    @Operation(summary = "Create a shop and assign a ROLE_SHOP user as owner")
    public ResponseEntity<ApiResponse<ShopResponse>> createShop(
            @Valid @RequestBody AdminCreateShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shop created", adminShopService.createShop(request)));
    }

    @GetMapping
    @Operation(summary = "List all shops")
    public ResponseEntity<ApiResponse<Page<ShopResponse>>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(ApiResponse.success(adminShopService.getAllShops(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a shop by ID")
    public ResponseEntity<ApiResponse<ShopResponse>> getShop(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminShopService.getShop(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shop details")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShop(
            @PathVariable String id,
            @Valid @RequestBody UpdateShopRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Shop updated", adminShopService.updateShop(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate / deactivate a shop")
    public ResponseEntity<ApiResponse<ShopResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateShopStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Shop status updated", adminShopService.updateStatus(id, request)));
    }
}
