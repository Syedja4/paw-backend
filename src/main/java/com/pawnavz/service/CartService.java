package com.pawnavz.service;

import com.pawnavz.dto.request.AddToCartRequest;
import com.pawnavz.dto.response.CartResponse;
import com.pawnavz.entity.*;
import com.pawnavz.exception.*;
import com.pawnavz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        return mapToResponse(getOrCreate(userId));
    }

    @Transactional
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        Cart cart = getOrCreate(userId);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(existing -> {
                    int newQty = existing.getQuantity() + request.getQuantity();
                    if (newQty > product.getStockQuantity()) {
                        throw new BadRequestException("Cannot add more. Max: " + product.getStockQuantity());
                    }
                    existing.setQuantity(newQty);
                    cartItemRepository.save(existing);
                }, () -> {
                    CartItem item = CartItem.builder().cart(cart).product(product)
                            .quantity(request.getQuantity()).build();
                    cart.getItems().add(item);
                    cartItemRepository.save(item);
                });
        return mapToResponse(cartRepository.findByUserIdWithItems(userId).orElse(cart));
    }

    @Transactional
    public CartResponse updateQuantity(String userId, String cartItemId, int quantity) {
        Cart cart = getOrCreate(userId);
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(cartItemId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));
        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new BadRequestException("Insufficient stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return mapToResponse(cartRepository.findByUserIdWithItems(userId).orElse(cart));
    }

    @Transactional
    public CartResponse removeFromCart(String userId, String cartItemId) {
        Cart cart = getOrCreate(userId);
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(cartItemId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return mapToResponse(cartRepository.findByUserIdWithItems(userId).orElse(cart));
    }

    @Transactional
    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private Cart getOrCreate(String userId) {
        return cartRepository.findByUserIdWithItems(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private CartResponse mapToResponse(Cart cart) {
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal delivery = subtotal.compareTo(new BigDecimal("499")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("49");
        List<CartResponse.CartItemResponse> items = cart.getItems().stream().map(i ->
                CartResponse.CartItemResponse.builder()
                        .cartItemId(i.getId()).productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .productImage(i.getProduct().getImageUrls().isEmpty()
                                ? null : i.getProduct().getImageUrls().get(0))
                        .unitPrice(i.getProduct().getPrice()).quantity(i.getQuantity())
                        .totalPrice(i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .stockQuantity(i.getProduct().getStockQuantity()).build()
        ).toList();
        return CartResponse.builder().cartId(cart.getId()).items(items)
                .totalItems(cart.getTotalItems()).subtotal(subtotal)
                .deliveryCharge(delivery).totalAmount(subtotal.add(delivery)).build();
    }
}
