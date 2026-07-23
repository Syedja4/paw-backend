package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A shop-partner's view of an order it must fulfill. Derived from the shared Order entity.
 */
@Data
@Builder
public class ShopOrderResponse {
    private String id;
    private String orderNumber;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;

    // Customer (limited fields relevant to fulfillment)
    private String customerName;
    private String customerPhone;
    private AddressResponse deliveryAddress;

    private List<OrderResponse.OrderItemResponse> items;

    // Delivery partner info (once requested by admin)
    private String deliveryPartner;
    private String deliveryTrackingId;
    private String deliveryStatus;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
