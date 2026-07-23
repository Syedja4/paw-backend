package com.pawnavz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignShopRequest {

    @NotBlank
    private String shopId;
}
