package com.pawnavz.service;

import com.pawnavz.dto.response.ProductResponse;
import com.pawnavz.entity.Pet;
import com.pawnavz.entity.Product;
import com.pawnavz.entity.User;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.exception.UnauthorizedException;
import com.pawnavz.repository.PetRepository;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getProductsWithFilters(String categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, String petType, String brand, Pageable pageable) {
        String type = petType != null ? petType.toUpperCase() : null;
        return productRepository.findWithFilters(categoryId, minPrice, maxPrice, type, brand, pageable)
                .map(this::mapToResponse);
    }

    public List<ProductResponse> getRecommendedProducts() {
        User currentUser = getCurrentUser();
        List<Pet> pets = petRepository.findByUserId(currentUser.getId());

        if (pets.isEmpty()) {
            Pageable featuredPage = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            return productRepository.findByIsFeaturedTrueAndIsActiveTrue(featuredPage)
                    .stream().map(this::mapToResponse).toList();
        }

        Set<String> petTypes = new LinkedHashSet<>();
        for (Pet pet : pets) {
            if (pet.getType() != null && !pet.getType().isBlank()) {
                petTypes.add(pet.getType().trim().toUpperCase());
            }
        }
        petTypes.add("ALL");

        return productRepository.findByPetTypeIn(new ArrayList<>(petTypes))
                .stream().map(this::mapToResponse).toList();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable).map(this::mapToResponse);
    }

    public Page<ProductResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable).map(this::mapToResponse);
    }

    public ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .description(p.getDescription()).price(p.getPrice()).mrp(p.getMrp())
                .discountPercent(p.getDiscountPercent()).brand(p.getBrand()).sku(p.getSku())
                .stockQuantity(p.getStockQuantity()).imageUrls(p.getImageUrls())
                .avgRating(p.getAvgRating()).reviewCount(p.getReviewCount())
                .isActive(p.getIsActive()).isFeatured(p.getIsFeatured())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .petType(p.getPetType())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }
}
