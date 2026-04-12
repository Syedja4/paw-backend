package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    @Pattern(regexp = "^(HOME|WORK|OTHER)$", message = "Label must be HOME, WORK, or OTHER")
    private String label;

    @Size(min = 2, max = 100)
    private String recipientName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 255)
    private String line1;

    @Size(max = 255)
    private String line2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;
}
