package com.pawnavz.service.admin;

import com.pawnavz.dto.request.CreateServiceAreaRequest;
import com.pawnavz.dto.response.ServiceAreaResponse;
import com.pawnavz.entity.Shop;
import com.pawnavz.entity.ShopServiceArea;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.ShopRepository;
import com.pawnavz.repository.ShopServiceAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceAreaService {

    private final ShopServiceAreaRepository serviceAreaRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public ServiceAreaResponse addServiceArea(String shopId, CreateServiceAreaRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", shopId));
        String pinCode = request.getPinCode().trim();
        if (serviceAreaRepository.existsByShopIdAndPinCode(shopId, pinCode)) {
            throw new ConflictException("PIN code " + pinCode + " is already assigned to this shop");
        }
        ShopServiceArea area = ShopServiceArea.builder()
                .shop(shop)
                .pinCode(pinCode)
                .city(request.getCity())
                .areaName(request.getAreaName())
                .build();
        return mapToResponse(serviceAreaRepository.save(area));
    }

    @Transactional(readOnly = true)
    public List<ServiceAreaResponse> getServiceAreas(String shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException("Shop", shopId);
        }
        return serviceAreaRepository.findByShopIdOrderByPinCodeAsc(shopId).stream()
                .map(this::mapToResponse).toList();
    }

    @Transactional
    public void deleteServiceArea(String shopId, String serviceAreaId) {
        ShopServiceArea area = serviceAreaRepository.findByIdAndShopId(serviceAreaId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Service area", serviceAreaId));
        serviceAreaRepository.delete(area);
    }

    private ServiceAreaResponse mapToResponse(ShopServiceArea area) {
        return ServiceAreaResponse.builder()
                .id(area.getId())
                .shopId(area.getShop() != null ? area.getShop().getId() : null)
                .pinCode(area.getPinCode())
                .city(area.getCity())
                .areaName(area.getAreaName())
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }
}
