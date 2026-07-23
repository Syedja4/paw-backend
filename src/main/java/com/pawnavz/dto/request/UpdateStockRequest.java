package com.pawnavz.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @NotNull
    @PositiveOrZero
    private Integer stock;
}
