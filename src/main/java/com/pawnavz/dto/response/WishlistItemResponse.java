package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WishlistItemResponse {
    private String productId;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercent;
    private String imageUrl;
    private Double avgRating;
    private Integer reviewCount;
    private Integer stockQuantity;
    private Boolean inStock;
}
