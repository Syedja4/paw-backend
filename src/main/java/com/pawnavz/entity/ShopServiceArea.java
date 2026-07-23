package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_service_areas",
        uniqueConstraints = @UniqueConstraint(name = "uk_shop_pincode", columnNames = {"shop_id", "pin_code"}),
        indexes = @Index(name = "idx_service_area_pincode", columnList = "pin_code"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ShopServiceArea extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "pin_code", nullable = false)
    private String pinCode;

    private String city;

    private String areaName;
}
