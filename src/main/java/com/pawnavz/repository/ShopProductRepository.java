package com.pawnavz.repository;

import com.pawnavz.entity.ShopProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopProductRepository extends JpaRepository<ShopProduct, String> {

    Page<ShopProduct> findByShopId(String shopId, Pageable pageable);

    Page<ShopProduct> findByShopIdAndAvailableTrue(String shopId, Pageable pageable);

    Optional<ShopProduct> findByIdAndShopId(String id, String shopId);

    boolean existsByShopIdAndProductId(String shopId, String productId);

    long countByShopId(String shopId);

    long countByShopIdAndAvailableTrue(String shopId);
}
