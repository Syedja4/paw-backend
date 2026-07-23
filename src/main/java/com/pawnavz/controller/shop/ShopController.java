package com.pawnavz.controller.shop;

import com.pawnavz.dto.request.UpdateShopRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ShopResponse;
import com.pawnavz.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shops")
@PreAuthorize("hasRole('SHOP')")
@RequiredArgsConstructor
@Tag(name = "Shop Partner")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated shop's profile")
    public ResponseEntity<ApiResponse<ShopResponse>> getMyShop() {
        return ResponseEntity.ok(ApiResponse.success(shopService.getMyShop()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated shop's profile")
    public ResponseEntity<ApiResponse<ShopResponse>> updateMyShop(
            @Valid @RequestBody UpdateShopRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Shop updated", shopService.updateMyShop(request)));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get shop dashboard metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(shopService.getDashboard()));
    }
}
