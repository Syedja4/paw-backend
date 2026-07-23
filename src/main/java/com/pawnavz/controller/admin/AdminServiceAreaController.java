package com.pawnavz.controller.admin;

import com.pawnavz.dto.request.CreateServiceAreaRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ServiceAreaResponse;
import com.pawnavz.service.admin.AdminServiceAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shops/{shopId}/service-areas")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Shop Service Areas")
public class AdminServiceAreaController {

    private final AdminServiceAreaService adminServiceAreaService;

    @PostMapping
    @Operation(summary = "Assign a PIN code (service area) to a shop")
    public ResponseEntity<ApiResponse<ServiceAreaResponse>> addServiceArea(
            @PathVariable String shopId,
            @Valid @RequestBody CreateServiceAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service area added",
                        adminServiceAreaService.addServiceArea(shopId, request)));
    }

    @GetMapping
    @Operation(summary = "List a shop's service areas (PIN codes)")
    public ResponseEntity<ApiResponse<List<ServiceAreaResponse>>> getServiceAreas(
            @PathVariable String shopId) {
        return ResponseEntity.ok(ApiResponse.success(adminServiceAreaService.getServiceAreas(shopId)));
    }

    @DeleteMapping("/{serviceAreaId}")
    @Operation(summary = "Remove a service area from a shop")
    public ResponseEntity<ApiResponse<Void>> deleteServiceArea(
            @PathVariable String shopId,
            @PathVariable String serviceAreaId) {
        adminServiceAreaService.deleteServiceArea(shopId, serviceAreaId);
        return ResponseEntity.ok(ApiResponse.success("Service area removed"));
    }
}
