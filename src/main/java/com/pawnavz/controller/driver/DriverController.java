package com.pawnavz.controller.driver;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.DriverResponse;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.service.driver.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/driver")
@PreAuthorize("hasRole('DRIVER')")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DriverResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(driverService.getMyProfile()));
    }

    @PatchMapping("/availability")
    public ResponseEntity<ApiResponse<DriverResponse>> updateAvailability(
            @RequestBody Map<String, Boolean> body) {
        Boolean available = body.get("available");
        if (available == null) throw new BadRequestException("'available' field is required");
        return ResponseEntity.ok(ApiResponse.success(
                "Availability updated", driverService.updateAvailability(available)));
    }
}
