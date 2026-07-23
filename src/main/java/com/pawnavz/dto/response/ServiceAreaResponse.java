package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServiceAreaResponse {
    private String id;
    private String shopId;
    private String pinCode;
    private String city;
    private String areaName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
