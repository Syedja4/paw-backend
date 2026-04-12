package com.pawnavz.controller;

import com.pawnavz.dto.request.AddToCartRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.CartResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(uid(auth))));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart",
                cartService.addToCart(uid(auth), request)));
    }

    @PatchMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @RequestHeader("Authorization") String auth,
            @PathVariable String cartItemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateQuantity(uid(auth), cartItemId, quantity)));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("Authorization") String auth,
            @PathVariable String cartItemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed",
                cartService.removeFromCart(uid(auth), cartItemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("Authorization") String auth) {
        cartService.clearCart(uid(auth));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
