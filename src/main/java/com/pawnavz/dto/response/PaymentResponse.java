package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String method;
    private String status;
    private String gatewayReference;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
