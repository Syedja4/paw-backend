package com.pawnavz.controller.driver;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.service.driver.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/driver/location")
@PreAuthorize("hasRole('DRIVER')")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateLocation(@RequestBody Map<String, Double> body) {
        Double lat = body.get("latitude");
        Double lng = body.get("longitude");
        if (lat == null || lng == null) throw new BadRequestException("latitude and longitude are required");
        driverLocationService.updateLocation(lat, lng);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully"));
    }
}
