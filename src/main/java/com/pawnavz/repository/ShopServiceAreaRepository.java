package com.pawnavz.repository;

import com.pawnavz.entity.Shop;
import com.pawnavz.entity.ShopServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopServiceAreaRepository extends JpaRepository<ShopServiceArea, String> {

    List<ShopServiceArea> findByShopIdOrderByPinCodeAsc(String shopId);

    Optional<ShopServiceArea> findByIdAndShopId(String id, String shopId);

    boolean existsByShopIdAndPinCode(String shopId, String pinCode);

    /**
     * All shops of the given status that serve a PIN code, ordered for deterministic selection.
     * Returned as a list so future logic (multiple shops per PIN, distance/GPS ranking,
     * automatic selection) can rank candidates without changing this query's contract.
     */
    @Query("SELECT sa.shop FROM ShopServiceArea sa " +
            "WHERE sa.pinCode = :pinCode AND sa.shop.status = :status " +
            "ORDER BY sa.shop.createdAt ASC")
    List<Shop> findShopsServingPinCode(@Param("pinCode") String pinCode,
                                       @Param("status") Shop.ShopStatus status);
}
