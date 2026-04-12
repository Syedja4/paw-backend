package com.pawnavz.dto.request;

import com.pawnavz.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String addressId;

    @NotNull
    private Order.PaymentMethod paymentMethod;

    private String couponCode;
    private String notes;
}
