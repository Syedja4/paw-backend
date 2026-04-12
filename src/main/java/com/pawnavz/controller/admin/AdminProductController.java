package com.pawnavz.controller.admin;

import com.pawnavz.dto.request.ProductRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.ProductResponse;
import com.pawnavz.service.admin.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Product created successfully", adminProductService.createProduct(request)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        System.out.println("API HIT: admin endpoint");
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return ResponseEntity.ok(ApiResponse.success(adminProductService.getAllProducts(search, categoryId, active, pageable)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success(adminProductService.getProductById(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id, @Valid @RequestBody ProductRequest request) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", adminProductService.updateProduct(id, request)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            adminProductService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProduct(@PathVariable String id) {
        System.out.println("API HIT: admin endpoint");
        try {
            return ResponseEntity.ok(ApiResponse.success("Product status toggled", adminProductService.toggleProduct(id)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
