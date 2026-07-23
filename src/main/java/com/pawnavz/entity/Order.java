package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address deliveryAddress;

    // Shop that fulfills this order. Null until an admin routes the order to a shop.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentReference;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime estimatedDelivery;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Third-party delivery (Porter / Ola) integration fields ────────────────
    // Populated when an admin requests delivery; kept provider-agnostic so a real
    // Porter/Ola integration can be plugged in without schema changes.
    @Enumerated(EnumType.STRING)
    private DeliveryPartner deliveryPartner;

    private String deliveryTrackingId;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }

    public enum PaymentMethod {
        COD, CARD, UPI, NET_BANKING, WALLET
    }

    public enum DeliveryPartner {
        PORTER, OLA, MANUAL
    }

    public enum DeliveryStatus {
        REQUESTED, ASSIGNED, PICKED_UP, DELIVERED, FAILED, CANCELLED
    }
}