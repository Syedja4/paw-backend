package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private AddressResponse deliveryAddress;
    private List<OrderItemResponse> items;
    private List<StatusHistoryResponse> timeline;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Driver info (populated for admin/driver views)
    private String driverId;
    private String driverName;
    private String driverPhone;
    private String driverVehicleNumber;

    // User info (populated for admin view)
    private String userId;
    private String userName;
    private String userPhone;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String productImage;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String returnStatus;
    }

    @Data
    @Builder
    public static class StatusHistoryResponse {
        private String status;
        private String description;
        private LocalDateTime changedAt;
    }
}