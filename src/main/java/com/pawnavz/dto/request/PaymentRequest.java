package com.pawnavz.dto.request;

import com.pawnavz.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank
    private String orderId;

    @NotNull
    private Order.PaymentMethod method;

    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;

    private String upiId;
    private String bankCode;
    private String walletProvider;
}
