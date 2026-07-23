package com.pawnavz.controller.shop;

import com.pawnavz.dto.request.AddShopProductRequest;
import com.pawnavz.dto.request.UpdatePriceRequest;
import com.pawnavz.dto.request.UpdateShopProductRequest;
import com.pawnavz.dto.request.UpdateStockRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ShopProductResponse;
import com.pawnavz.service.ShopProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops/products")
@PreAuthorize("hasRole('SHOP')")
@RequiredArgsConstructor
@Tag(name = "Shop Products")
public class ShopProductController {

    private final ShopProductService shopProductService;

    @GetMapping
    @Operation(summary = "List the shop's products")
    public ResponseEntity<ApiResponse<Page<ShopProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(shopProductService.getMyProducts(pageable)));
    }

    @PostMapping
    @Operation(summary = "List a catalog product in the shop")
    public ResponseEntity<ApiResponse<ShopProductResponse>> addProduct(
            @Valid @RequestBody AddShopProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added", shopProductService.addProduct(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shop product (price / stock / availability)")
    public ResponseEntity<ApiResponse<ShopProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateShopProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated",
                shopProductService.updateProduct(id, request)));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update stock for a shop product")
    public ResponseEntity<ApiResponse<ShopProductResponse>> updateStock(
            @PathVariable String id,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated",
                shopProductService.updateStock(id, request)));
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update price for a shop product")
    public ResponseEntity<ApiResponse<ShopProductResponse>> updatePrice(
            @PathVariable String id,
            @Valid @RequestBody UpdatePriceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Price updated",
                shopProductService.updatePrice(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a product from the shop")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        shopProductService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed"));
    }
}
