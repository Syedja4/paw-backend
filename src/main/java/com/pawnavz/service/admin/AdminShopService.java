package com.pawnavz.service.admin;

import com.pawnavz.dto.request.AdminCreateShopRequest;
import com.pawnavz.dto.request.UpdateShopRequest;
import com.pawnavz.dto.request.UpdateShopStatusRequest;
import com.pawnavz.dto.response.ShopResponse;
import com.pawnavz.entity.Shop;
import com.pawnavz.entity.User;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.ShopRepository;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ShopService shopService;

    /** Creates a shop and assigns an existing ROLE_SHOP user as its owner. */
    @Transactional
    public ShopResponse createShop(AdminCreateShopRequest request) {
        User owner = userRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getOwnerUserId()));
        if (owner.getRole() != User.Role.SHOP) {
            throw new BadRequestException("Owner user must have ROLE_SHOP");
        }
        if (shopRepository.existsByOwnerId(owner.getId())) {
            throw new ConflictException("This user already owns a shop");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && shopRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("A shop with this email already exists");
        }

        Shop shop = Shop.builder()
                .owner(owner)
                .shopName(request.getShopName())
                .ownerName(request.getOwnerName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gstNumber(request.getGstNumber())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(Shop.ShopStatus.ACTIVE)
                .build();
        return shopService.toResponse(shopRepository.save(shop));
    }

    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable).map(shopService::toResponse);
    }

    @Transactional(readOnly = true)
    public ShopResponse getShop(String id) {
        return shopService.toResponse(findById(id));
    }

    @Transactional
    public ShopResponse updateShop(String id, UpdateShopRequest request) {
        Shop shop = findById(id);
        if (request.getShopName() != null) shop.setShopName(request.getShopName());
        if (request.getOwnerName() != null) shop.setOwnerName(request.getOwnerName());
        if (request.getPhone() != null) shop.setPhone(request.getPhone());
        if (request.getGstNumber() != null) shop.setGstNumber(request.getGstNumber());
        if (request.getAddress() != null) shop.setAddress(request.getAddress());
        if (request.getLatitude() != null) shop.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) shop.setLongitude(request.getLongitude());
        return shopService.toResponse(shopRepository.save(shop));
    }

    @Transactional
    public ShopResponse updateStatus(String id, UpdateShopStatusRequest request) {
        Shop shop = findById(id);
        shop.setStatus(request.getStatus());
        return shopService.toResponse(shopRepository.save(shop));
    }

    private Shop findById(String id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", id));
    }
}
