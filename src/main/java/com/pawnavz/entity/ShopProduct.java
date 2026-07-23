package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shop_products",
        uniqueConstraints = @UniqueConstraint(name = "uk_shop_product", columnNames = {"shop_id", "product_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ShopProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // References the global catalog Product; shops set their own price/stock/availability.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;
}
