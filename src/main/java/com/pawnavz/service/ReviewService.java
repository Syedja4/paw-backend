package com.pawnavz.service;

import com.pawnavz.dto.request.AddReviewRequest;
import com.pawnavz.dto.request.UpdateReviewRequest;
import com.pawnavz.dto.response.ReviewResponse;
import com.pawnavz.dto.response.ReviewSummaryResponse;
import com.pawnavz.entity.Product;
import com.pawnavz.entity.Review;
import com.pawnavz.entity.User;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.ReviewRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(String productId, Pageable pageable) {
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product", productId);
        return reviewRepository
                .findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getProductSummary(String productId) {
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product", productId);
        Double avg = reviewRepository.findAvgRatingByProductId(productId);
        long total = reviewRepository.countApprovedByProductId(productId);
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) breakdown.put(i, 0L);
        reviewRepository.findRatingBreakdownByProductId(productId)
                .forEach(row -> breakdown.put(((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue()));
        return ReviewSummaryResponse.builder()
                .productId(productId)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(total).ratingBreakdown(breakdown).build();
    }

    @Transactional
    public ReviewResponse addReview(String userId, AddReviewRequest req) {
        if (reviewRepository.existsByProductIdAndUserId(req.getProductId(), userId))
            throw new ConflictException("You have already reviewed this product");
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Review review = Review.builder()
                .product(product).user(user).rating(req.getRating())
                .title(req.getTitle() != null ? req.getTitle().trim() : null)
                .comment(req.getComment() != null ? req.getComment().trim() : null)
                .isVerifiedPurchase(false).isApproved(true).helpfulCount(0).build();
        Review saved = reviewRepository.save(review);
        syncProductStats(product);
        return mapToResponse(saved);
    }

    @Transactional
    public ReviewResponse updateReview(String userId, String reviewId, UpdateReviewRequest req) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found or does not belong to current user"));
        boolean changed = false;
        if (req.getRating() != null) { review.setRating(req.getRating()); changed = true; }
        if (req.getTitle() != null) { review.setTitle(req.getTitle().trim()); changed = true; }
        if (req.getComment() != null) { review.setComment(req.getComment().trim()); changed = true; }
        if (!changed) throw new BadRequestException("No valid fields provided for update");
        Review saved = reviewRepository.save(review);
        if (req.getRating() != null) syncProductStats(review.getProduct());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteReview(String userId, String reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found or does not belong to current user"));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        syncProductStats(product);
    }

    private void syncProductStats(Product product) {
        Double avg = reviewRepository.findAvgRatingByProductId(product.getId());
        long count = reviewRepository.countApprovedByProductId(product.getId());
        product.setAvgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        product.setReviewCount((int) count);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId()).productId(r.getProduct().getId())
                .userId(r.getUser().getId()).userName(r.getUser().getName())
                .userAvatar(r.getUser().getProfileImageUrl()).rating(r.getRating())
                .title(r.getTitle()).comment(r.getComment())
                .isVerifiedPurchase(r.getIsVerifiedPurchase())
                .helpfulCount(r.getHelpfulCount())
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }
}
