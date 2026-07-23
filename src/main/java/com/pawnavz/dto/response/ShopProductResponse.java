package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShopProductResponse {
    private String id;
    private String shopId;

    // Catalog product details
    private String productId;
    private String productName;
    private String productImage;
    private String brand;
    private BigDecimal catalogPrice;

    // Shop-specific offering
    private BigDecimal price;
    private Integer stock;
    private Boolean available;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
