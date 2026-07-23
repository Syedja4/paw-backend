package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Location-aware product listing. When no ACTIVE shop serves the requested PIN code,
 * {@code serviceAvailable} is false, {@code products} is empty and {@code message} explains why.
 */
@Data
@Builder
public class ProductAvailabilityResponse {
    private boolean serviceAvailable;
    private String message;
    private String shopId;
    private String shopName;
    private String pinCode;

    @Builder.Default
    private List<ProductResponse> products = List.of();

    private long totalProducts;
    private int page;
    private int size;
}
