package com.pawnavz.controller;

import com.pawnavz.dto.request.AddReviewRequest;
import com.pawnavz.dto.request.UpdateReviewRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ReviewResponse;
import com.pawnavz.dto.response.ReviewSummaryResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get paginated reviews for a product")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getProductReviews(productId, pageable)));
    }

    @GetMapping("/product/{productId}/summary")
    @Operation(summary = "Get rating summary for a product")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getSummary(
            @PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getProductSummary(productId)));
    }

    @PostMapping
    @Operation(summary = "Add a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody AddReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Review submitted",
                        reviewService.addReview(uid(auth), request)));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update your review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @RequestHeader("Authorization") String auth,
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review updated",
                reviewService.updateReview(uid(auth), reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete your review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @RequestHeader("Authorization") String auth,
            @PathVariable String reviewId) {
        reviewService.deleteReview(uid(auth), reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }
}
