package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShopResponse {
    private String id;
    private String ownerId;
    private String shopName;
    private String ownerName;
    private String phone;
    private String email;
    private String gstNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
