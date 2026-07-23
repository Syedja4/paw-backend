package com.pawnavz.repository;

import com.pawnavz.entity.ShopProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ShopProductRepository extends JpaRepository<ShopProduct, String> {

    Page<ShopProduct> findByShopId(String shopId, Pageable pageable);

    Page<ShopProduct> findByShopIdAndAvailableTrue(String shopId, Pageable pageable);

    /**
     * Filtered listing scoped to a SINGLE shop's available products. Catalog attributes
     * (category, brand, pet type, name search) filter on the linked Product; price filters
     * use the shop's own price. Never crosses shop boundaries.
     */
    @Query("SELECT sp FROM ShopProduct sp JOIN sp.product p " +
            "WHERE sp.shop.id = :shopId AND sp.available = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) " +
            "AND (:petType IS NULL OR LOWER(p.petType) = LOWER(:petType)) " +
            "AND (:minPrice IS NULL OR sp.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR sp.price <= :maxPrice) " +
            "AND (:search IS NULL " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ShopProduct> findShopCatalog(@Param("shopId") String shopId,
                                      @Param("categoryId") String categoryId,
                                      @Param("brand") String brand,
                                      @Param("petType") String petType,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      @Param("search") String search,
                                      Pageable pageable);

    Optional<ShopProduct> findByIdAndShopId(String id, String shopId);

    boolean existsByShopIdAndProductId(String shopId, String productId);

    long countByShopId(String shopId);

    long countByShopIdAndAvailableTrue(String shopId);
}
