package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercent;
    private String brand;
    private String sku;
    private Integer stockQuantity;
    private List<String> imageUrls;
    private Double avgRating;
    private Integer reviewCount;
    private Boolean isActive;
    private Boolean isFeatured;
    private String categoryId;
    private String categoryName;
    private String petType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}