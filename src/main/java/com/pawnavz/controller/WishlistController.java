package com.pawnavz.controller;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.WishlistResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @GetMapping
    @Operation(summary = "Get current user's wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getWishlist(uid(auth))));
    }

    @PostMapping("/items/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addProduct(
            @RequestHeader("Authorization") String auth,
            @PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist",
                wishlistService.addProduct(uid(auth), productId)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeProduct(
            @RequestHeader("Authorization") String auth,
            @PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist",
                wishlistService.removeProduct(uid(auth), productId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear wishlist")
    public ResponseEntity<ApiResponse<Void>> clear(
            @RequestHeader("Authorization") String auth) {
        wishlistService.clearWishlist(uid(auth));
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared"));
    }
}
