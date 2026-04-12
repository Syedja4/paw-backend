package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotBlank
    private String productId;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer quantity;
}
