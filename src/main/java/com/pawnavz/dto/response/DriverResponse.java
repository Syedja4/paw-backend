package com.pawnavz.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private Long id;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String vehicleNumber;
    private String vehicleType;
    private String licenseNumber;
    private String status;
    private boolean available;
    private Double latitude;
    private Double longitude;
    private BigDecimal totalEarnings;
    private LocalDateTime createdAt;
}
