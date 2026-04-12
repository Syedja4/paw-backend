package com.pawnavz.service.admin;

import com.pawnavz.dto.request.ProductRequest;
import com.pawnavz.dto.response.ProductResponse;
import com.pawnavz.entity.Category;
import com.pawnavz.entity.Product;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.CategoryRepository;
import com.pawnavz.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        System.out.println("ADMIN API HIT");
        try {
            Category category;

            if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName("Default Category");
                            return categoryRepository.save(c);
                        });
            } else {
                category = categoryRepository.findByName("Default Category")
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName("Default Category");
                            return categoryRepository.save(c);
                        });
            }

            Product product = Product.builder()
                    .name(request.getName())
                    .slug(toSlug(request.getName()))
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .stockQuantity(request.getStockQuantity())
                    .brand(request.getBrand())
                    .category(category)
                    .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>())
                    .isActive(request.getIsActive())
                    .isFeatured(request.getIsFeatured())
                    .build();

            return mapToResponse(productRepository.save(product));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String search, String categoryId, Boolean active, Pageable pageable) {
        System.out.println("ADMIN API HIT - search=" + search + " categoryId=" + categoryId);

        Page<Product> products = productRepository.findAll(pageable);

        System.out.println("PRODUCTS FOUND: " + products.getTotalElements());

        return products.map(this::mapToResponse);
    }

    public ProductResponse getProductById(String id) {
        System.out.println("ADMIN API HIT");
        try {
            return mapToResponse(findById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return emptyProductResponse();
        }
    }

    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest request) {
        System.out.println("ADMIN API HIT");
        try {
            Product product = findById(id);
            Category category;

            if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName("Default Category");
                            return categoryRepository.save(c);
                        });
            } else {
                category = categoryRepository.findByName("Default Category")
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName("Default Category");
                            return categoryRepository.save(c);
                        });
            }

            product.setName(request.getName());
            if (product.getSlug() == null || product.getSlug().isBlank()) {
                product.setSlug(toSlug(request.getName()));
            }
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStockQuantity(request.getStockQuantity());
            product.setBrand(request.getBrand());
            product.setCategory(category);
            product.setImageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>());
            product.setIsActive(request.getIsActive());
            product.setIsFeatured(request.getIsFeatured());

            return mapToResponse(productRepository.save(product));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void deleteProduct(String id) {
        System.out.println("ADMIN API HIT");
        try {
            productRepository.delete(findById(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public ProductResponse toggleProduct(String id) {
        System.out.println("ADMIN API HIT");
        try {
            Product product = findById(id);
            product.setIsActive(!Boolean.TRUE.equals(product.getIsActive()));
            return mapToResponse(productRepository.save(product));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(Optional.ofNullable(product.getId()).orElse(""))
                .name(Optional.ofNullable(product.getName()).orElse(""))
                .slug(Optional.ofNullable(product.getSlug()).orElse(""))
                .description(Optional.ofNullable(product.getDescription()).orElse(""))
                .price(Optional.ofNullable(product.getPrice()).orElse(BigDecimal.ZERO))
                .mrp(Optional.ofNullable(product.getMrp()).orElse(BigDecimal.ZERO))
                .discountPercent(Optional.ofNullable(product.getDiscountPercent()).orElse(0))
                .brand(Optional.ofNullable(product.getBrand()).orElse(""))
                .sku(Optional.ofNullable(product.getSku()).orElse(""))
                .stockQuantity(Optional.ofNullable(product.getStockQuantity()).orElse(0))
                .imageUrls(Optional.ofNullable(product.getImageUrls()).orElse(Collections.emptyList()))
                .avgRating(Optional.ofNullable(product.getAvgRating()).orElse(0.0))
                .reviewCount(Optional.ofNullable(product.getReviewCount()).orElse(0))
                .isActive(Optional.ofNullable(product.getIsActive()).orElse(Boolean.FALSE))
                .isFeatured(Optional.ofNullable(product.getIsFeatured()).orElse(Boolean.FALSE))
                .categoryId(product.getCategory() != null ? Optional.ofNullable(product.getCategory().getId()).orElse("") : "")
                .categoryName(product.getCategory() != null ? Optional.ofNullable(product.getCategory().getName()).orElse("") : "")
                .petType(Optional.ofNullable(product.getPetType()).orElse(""))
                .createdAt(Optional.ofNullable(product.getCreatedAt()).orElse(LocalDateTime.now()))
                .updatedAt(Optional.ofNullable(product.getUpdatedAt()).orElse(LocalDateTime.now()))
                .build();
    }

    private ProductResponse emptyProductResponse() {
        return ProductResponse.builder()
                .id("").name("").slug("").description("")
                .price(BigDecimal.ZERO).mrp(BigDecimal.ZERO).discountPercent(0)
                .brand("").sku("").stockQuantity(0)
                .imageUrls(Collections.emptyList())
                .avgRating(0.0).reviewCount(0)
                .isActive(Boolean.FALSE).isFeatured(Boolean.FALSE)
                .categoryId("").categoryName("").petType("")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private String toSlug(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9\\s-]", "");
        normalized = normalized.replaceAll("\\s+", "-");
        normalized = normalized.replaceAll("-{2,}", "-");
        return normalized;
    }
}