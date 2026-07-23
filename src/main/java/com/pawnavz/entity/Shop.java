package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shops")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Shop extends BaseEntity {

    // Owner (shop-partner) account used for authentication. The linked User carries role SHOP.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false)
    private String shopName;

    private String ownerName;

    private String phone;

    @Column(unique = true)
    private String email;

    private String gstNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShopStatus status = ShopStatus.ACTIVE;

    public enum ShopStatus {
        ACTIVE, INACTIVE
    }
}
