package com.pawnavz.service;

import com.pawnavz.dto.request.AddShopProductRequest;
import com.pawnavz.dto.request.UpdatePriceRequest;
import com.pawnavz.dto.request.UpdateShopProductRequest;
import com.pawnavz.dto.request.UpdateStockRequest;
import com.pawnavz.dto.response.ShopProductResponse;
import com.pawnavz.entity.Product;
import com.pawnavz.entity.Shop;
import com.pawnavz.entity.ShopProduct;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.ShopProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopProductService {

    private final ShopProductRepository shopProductRepository;
    private final ProductRepository productRepository;
    private final ShopService shopService;

    @Transactional(readOnly = true)
    public Page<ShopProductResponse> getMyProducts(Pageable pageable) {
        Shop shop = shopService.getCurrentShop();
        return shopProductRepository.findByShopId(shop.getId(), pageable).map(this::mapToResponse);
    }

    @Transactional
    public ShopProductResponse addProduct(AddShopProductRequest request) {
        Shop shop = shopService.getCurrentShop();
        if (shopProductRepository.existsByShopIdAndProductId(shop.getId(), request.getProductId())) {
            throw new ConflictException("This product is already listed by your shop");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        ShopProduct shopProduct = ShopProduct.builder()
                .shop(shop)
                .product(product)
                .price(request.getPrice())
                .stock(request.getStock())
                .available(request.getAvailable() == null ? Boolean.TRUE : request.getAvailable())
                .build();
        return mapToResponse(shopProductRepository.save(shopProduct));
    }

    @Transactional
    public ShopProductResponse updateProduct(String id, UpdateShopProductRequest request) {
        ShopProduct shopProduct = findOwned(id);
        if (request.getPrice() != null) shopProduct.setPrice(request.getPrice());
        if (request.getStock() != null) shopProduct.setStock(request.getStock());
        if (request.getAvailable() != null) shopProduct.setAvailable(request.getAvailable());
        return mapToResponse(shopProductRepository.save(shopProduct));
    }

    @Transactional
    public ShopProductResponse updateStock(String id, UpdateStockRequest request) {
        ShopProduct shopProduct = findOwned(id);
        shopProduct.setStock(request.getStock());
        return mapToResponse(shopProductRepository.save(shopProduct));
    }

    @Transactional
    public ShopProductResponse updatePrice(String id, UpdatePriceRequest request) {
        ShopProduct shopProduct = findOwned(id);
        shopProduct.setPrice(request.getPrice());
        return mapToResponse(shopProductRepository.save(shopProduct));
    }

    @Transactional
    public void deleteProduct(String id) {
        ShopProduct shopProduct = findOwned(id);
        shopProductRepository.delete(shopProduct);
    }

    private ShopProduct findOwned(String id) {
        Shop shop = shopService.getCurrentShop();
        return shopProductRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop product", id));
    }

    private ShopProductResponse mapToResponse(ShopProduct sp) {
        Product p = sp.getProduct();
        return ShopProductResponse.builder()
                .id(sp.getId())
                .shopId(sp.getShop() != null ? sp.getShop().getId() : null)
                .productId(p != null ? p.getId() : null)
                .productName(p != null ? p.getName() : null)
                .productImage(p != null && p.getImageUrls() != null && !p.getImageUrls().isEmpty()
                        ? p.getImageUrls().get(0) : null)
                .brand(p != null ? p.getBrand() : null)
                .catalogPrice(p != null ? p.getPrice() : null)
                .price(sp.getPrice())
                .stock(sp.getStock())
                .available(sp.getAvailable())
                .createdAt(sp.getCreatedAt())
                .updatedAt(sp.getUpdatedAt())
                .build();
    }
}
