package com.pawnavz.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateShopRequest {

    private String shopName;

    private String ownerName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    private String gstNumber;

    private String address;

    private Double latitude;

    private Double longitude;
}
