package com.pawnavz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateServiceAreaRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN code must be a 6-digit number")
    private String pinCode;

    private String city;

    private String areaName;
}
