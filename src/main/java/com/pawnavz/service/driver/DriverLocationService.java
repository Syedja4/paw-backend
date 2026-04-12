package com.pawnavz.service.driver;

import com.pawnavz.entity.Driver;
import com.pawnavz.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final DriverRepository driverRepository;
    private final DriverService driverService;

    @Transactional
    public void updateLocation(Double latitude, Double longitude) {
        Driver driver = driverService.getCurrentDriver();
        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        driverRepository.save(driver);
    }
}