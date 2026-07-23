package com.pawnavz.controller;

import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ProductAvailabilityResponse;
import com.pawnavz.dto.response.ProductResponse;
import com.pawnavz.exception.UnauthorizedException;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "List products from the shop serving the customer's selected/default "
            + "delivery address, with optional filters applied to that shop only")
    public ResponseEntity<ApiResponse<ProductAvailabilityResponse>> getProducts(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(required = false) String addressId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authentication required");
        }
        String userId = jwtUtil.extractUserId(auth.substring(7));
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), parseSort(sort));
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductsForDeliveryAddress(
                        userId, addressId, categoryId, brand, petType, minPrice, maxPrice, search, pageable)));
    }

    /** Maps the public {@code sort} token to a safe, whitelisted Sort over shop-product fields. */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("createdAt").descending();
        }
        return switch (sort.trim().toLowerCase()) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name_asc" -> Sort.by("product.name").ascending();
            case "name_desc" -> Sort.by("product.name").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(q, PageRequest.of(page, size))));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getFeatured(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getFeaturedProducts(PageRequest.of(page, size))));
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_DRIVER')")
    @Operation(summary = "Get recommended products for current user")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendations() {
        return ResponseEntity.ok(ApiResponse.success(productService.getRecommendedProducts()));
    }
}
