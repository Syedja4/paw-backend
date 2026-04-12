package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank(message = "Label is required")
    @Pattern(regexp = "^(HOME|WORK|OTHER)$", message = "Label must be HOME, WORK, or OTHER")
    private String label;

    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100)
    private String recipientName;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String line1;

    @Size(max = 255)
    private String line2;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Size(max = 100)
    private String state;

    @NotBlank
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
    private String pincode;

    private Boolean isDefault = false;
}
