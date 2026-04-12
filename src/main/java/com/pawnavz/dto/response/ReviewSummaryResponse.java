package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReviewSummaryResponse {
    private String productId;
    private Double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingBreakdown;
}
