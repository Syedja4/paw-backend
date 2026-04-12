package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Order.PaymentStatus status = Order.PaymentStatus.PENDING;

    private String gatewayReference;

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    private String failureReason;

    private LocalDateTime paidAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;

    private LocalDateTime refundedAt;
}
