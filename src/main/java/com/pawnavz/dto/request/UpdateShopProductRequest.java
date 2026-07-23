package com.pawnavz.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/** Full update of a shop's product offering (price / stock / availability). */
@Data
public class UpdateShopProductRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @PositiveOrZero
    private Integer stock;

    private Boolean available;
}
