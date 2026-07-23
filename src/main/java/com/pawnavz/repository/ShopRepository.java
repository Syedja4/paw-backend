package com.pawnavz.repository;

import com.pawnavz.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, String> {

    Optional<Shop> findByOwnerId(String ownerId);

    Optional<Shop> findByOwnerEmail(String email);

    boolean existsByOwnerId(String ownerId);

    boolean existsByEmail(String email);

    long countByStatus(Shop.ShopStatus status);
}
