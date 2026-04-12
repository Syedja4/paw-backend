package com.pawnavz.service;

import com.pawnavz.dto.response.WishlistItemResponse;
import com.pawnavz.dto.response.WishlistResponse;
import com.pawnavz.entity.Product;
import com.pawnavz.entity.User;
import com.pawnavz.entity.Wishlist;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String userId) {
        return mapToResponse(getOrCreate(userId));
    }

    @Transactional
    public WishlistResponse addProduct(String userId, String productId) {
        Wishlist wishlist = getOrCreate(userId);
        if (wishlist.getProducts().stream().anyMatch(p -> p.getId().equals(productId))) {
            throw new BadRequestException("Product is already in your wishlist");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!product.getIsActive()) throw new BadRequestException("Product is no longer available");
        wishlist.getProducts().add(product);
        return mapToResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public WishlistResponse removeProduct(String userId, String productId) {
        Wishlist wishlist = getOrCreate(userId);
        if (!wishlist.getProducts().removeIf(p -> p.getId().equals(productId))) {
            throw new ResourceNotFoundException("Product not found in wishlist");
        }
        return mapToResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public void clearWishlist(String userId) {
        Wishlist wishlist = getOrCreate(userId);
        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);
    }

    private Wishlist getOrCreate(String userId) {
        return wishlistRepository.findByUserIdWithProducts(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            return wishlistRepository.save(Wishlist.builder().user(user).build());
        });
    }

    private WishlistResponse mapToResponse(Wishlist wishlist) {
        var items = wishlist.getProducts().stream().map(p ->
                WishlistItemResponse.builder()
                        .productId(p.getId()).name(p.getName()).slug(p.getSlug())
                        .brand(p.getBrand()).price(p.getPrice()).mrp(p.getMrp())
                        .discountPercent(p.getDiscountPercent())
                        .imageUrl(p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0))
                        .avgRating(p.getAvgRating()).reviewCount(p.getReviewCount())
                        .stockQuantity(p.getStockQuantity())
                        .inStock(p.getStockQuantity() > 0).build()
        ).toList();
        return WishlistResponse.builder()
                .wishlistId(wishlist.getId()).totalItems(items.size()).items(items).build();
    }
}
