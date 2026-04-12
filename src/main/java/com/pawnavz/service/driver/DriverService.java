package com.pawnavz.service.driver;

import com.pawnavz.dto.response.DriverResponse;
import com.pawnavz.entity.Driver;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse getMyProfile() {
        return mapToResponse(getCurrentDriver());
    }

    @Transactional
    public DriverResponse updateAvailability(boolean available) {
        Driver driver = getCurrentDriver();
        driver.setAvailable(available);
        return mapToResponse(driverRepository.save(driver));
    }

    public Driver getCurrentDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return driverRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for: " + email));
    }

    public DriverResponse mapToResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUser().getId())
                .name(driver.getUser().getName())
                .email(driver.getUser().getEmail())
                .phone(driver.getUser().getPhone())
                .vehicleNumber(driver.getVehicleNumber())
                .vehicleType(driver.getVehicleType())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus())
                .available(driver.isAvailable())
                .latitude(driver.getLatitude())
                .longitude(driver.getLongitude())
                .totalEarnings(driver.getTotalEarnings())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}