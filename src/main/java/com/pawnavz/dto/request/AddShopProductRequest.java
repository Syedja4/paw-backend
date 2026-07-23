package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddShopProductRequest {

    @NotBlank
    private String productId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private Boolean available;
}
