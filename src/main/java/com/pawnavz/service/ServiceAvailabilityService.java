package com.pawnavz.service;

import com.pawnavz.entity.Shop;
import com.pawnavz.repository.ShopServiceAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Central decision point for "is Pawnavz available at this PIN code, and which shop serves it?".
 * <p>
 * All PIN-based routing goes through here so future capabilities — multiple shops per PIN,
 * automatic shop selection, delivery radius, GPS-based service areas — can be added by
 * changing only {@link #selectServingShop} without touching callers (products, orders, admin).
 */
@Service
@RequiredArgsConstructor
public class ServiceAvailabilityService {

    private final ShopServiceAreaRepository serviceAreaRepository;

    /** Returns the shop that should fulfill orders for the given PIN code, if any. */
    @Transactional(readOnly = true)
    public Optional<Shop> findServingShop(String pinCode) {
        if (pinCode == null || pinCode.isBlank()) {
            return Optional.empty();
        }
        List<Shop> candidates = serviceAreaRepository
                .findShopsServingPinCode(pinCode.trim(), Shop.ShopStatus.ACTIVE);
        return selectServingShop(candidates);
    }

    /** True when at least one ACTIVE shop serves the PIN code. */
    @Transactional(readOnly = true)
    public boolean isServiceable(String pinCode) {
        return findServingShop(pinCode).isPresent();
    }

    /**
     * Chooses one shop among the candidates serving a PIN code.
     * <p>
     * MVP rule: assume one active shop per PIN and pick the first (oldest) candidate.
     * This is the extension seam for multi-shop / distance / GPS selection later.
     */
    private Optional<Shop> selectServingShop(List<Shop> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(0));
    }
}
