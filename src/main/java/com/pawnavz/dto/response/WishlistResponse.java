package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WishlistResponse {
    private String wishlistId;
    private int totalItems;
    private List<WishlistItemResponse> items;
}
