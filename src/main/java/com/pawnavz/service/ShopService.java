package com.pawnavz.service;

import com.pawnavz.dto.request.UpdateShopRequest;
import com.pawnavz.dto.response.ShopResponse;
import com.pawnavz.entity.Order;
import com.pawnavz.entity.Shop;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.OrderRepository;
import com.pawnavz.repository.ShopProductRepository;
import com.pawnavz.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopProductRepository shopProductRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public ShopResponse getMyShop() {
        return toResponse(getCurrentShop());
    }

    @Transactional
    public ShopResponse updateMyShop(UpdateShopRequest request) {
        Shop shop = getCurrentShop();
        if (request.getShopName() != null) shop.setShopName(request.getShopName());
        if (request.getOwnerName() != null) shop.setOwnerName(request.getOwnerName());
        if (request.getPhone() != null) shop.setPhone(request.getPhone());
        if (request.getGstNumber() != null) shop.setGstNumber(request.getGstNumber());
        if (request.getAddress() != null) shop.setAddress(request.getAddress());
        if (request.getLatitude() != null) shop.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) shop.setLongitude(request.getLongitude());
        return toResponse(shopRepository.save(shop));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        Shop shop = getCurrentShop();
        String shopId = shop.getId();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("shopId", shopId);
        stats.put("shopName", shop.getShopName());
        stats.put("status", shop.getStatus().name());
        stats.put("totalProducts", shopProductRepository.countByShopId(shopId));
        stats.put("availableProducts", shopProductRepository.countByShopIdAndAvailableTrue(shopId));
        stats.put("totalOrders", orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, Pageable.unpaged()).getTotalElements());
        stats.put("pendingOrders", countByStatus(shopId, Order.OrderStatus.PENDING));
        stats.put("confirmedOrders", countByStatus(shopId, Order.OrderStatus.CONFIRMED));
        stats.put("readyOrders", countByStatus(shopId, Order.OrderStatus.PROCESSING));
        stats.put("deliveredOrders", countByStatus(shopId, Order.OrderStatus.DELIVERED));
        stats.put("cancelledOrders", countByStatus(shopId, Order.OrderStatus.CANCELLED));
        return stats;
    }

    private long countByStatus(String shopId, Order.OrderStatus status) {
        return orderRepository
                .findByShopIdAndStatusOrderByCreatedAtDesc(shopId, status, Pageable.unpaged())
                .getTotalElements();
    }

    /** Resolves the shop owned by the currently authenticated (SHOP-role) user. */
    public Shop getCurrentShop() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return shopRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Shop profile not found for: " + email));
    }

    /** Shared Shop → ShopResponse mapper, reused by the admin shop-management layer. */
    public ShopResponse toResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .ownerId(shop.getOwner() != null ? shop.getOwner().getId() : null)
                .shopName(shop.getShopName())
                .ownerName(shop.getOwnerName())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .gstNumber(shop.getGstNumber())
                .address(shop.getAddress())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .status(shop.getStatus() != null ? shop.getStatus().name() : null)
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }
}
