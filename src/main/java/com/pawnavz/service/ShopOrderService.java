package com.pawnavz.service;

import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.dto.response.ShopOrderResponse;
import com.pawnavz.entity.Address;
import com.pawnavz.entity.Order;
import com.pawnavz.entity.OrderStatusHistory;
import com.pawnavz.entity.Shop;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.OrderRepository;
import com.pawnavz.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShopService shopService;

    @Transactional(readOnly = true)
    public Page<ShopOrderResponse> getMyOrders(String status, Pageable pageable) {
        Shop shop = shopService.getCurrentShop();
        Order.OrderStatus parsed = parseStatus(status);
        Page<Order> orders = parsed == null
                ? orderRepository.findByShopIdOrderByCreatedAtDesc(shop.getId(), pageable)
                : orderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shop.getId(), parsed, pageable);
        return orders.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ShopOrderResponse getOrderById(String orderId) {
        return mapToResponse(findOwned(orderId));
    }

    @Transactional
    public ShopOrderResponse acceptOrder(String orderId) {
        Order order = findOwned(orderId);
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be accepted (current: " + order.getStatus() + ")");
        }
        order.setStatus(Order.OrderStatus.CONFIRMED);
        addHistory(order, Order.OrderStatus.CONFIRMED, "Order accepted by shop");
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public ShopOrderResponse rejectOrder(String orderId) {
        Order order = findOwned(orderId);
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Only PENDING or CONFIRMED orders can be rejected (current: " + order.getStatus() + ")");
        }
        // Restore catalog stock for each item.
        order.getItems().forEach(item -> {
            if (item.getProduct() != null) {
                item.getProduct().setStockQuantity(item.getProduct().getStockQuantity() + item.getQuantity());
                productRepository.save(item.getProduct());
            }
        });
        order.setStatus(Order.OrderStatus.CANCELLED);
        addHistory(order, Order.OrderStatus.CANCELLED, "Order rejected by shop");
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public ShopOrderResponse markReady(String orderId) {
        Order order = findOwned(orderId);
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED orders can be marked ready (current: " + order.getStatus() + ")");
        }
        order.setStatus(Order.OrderStatus.PROCESSING);
        addHistory(order, Order.OrderStatus.PROCESSING, "Order packed and ready for pickup");
        return mapToResponse(orderRepository.save(order));
    }

    private Order findOwned(String orderId) {
        Shop shop = shopService.getCurrentShop();
        return orderRepository.findByIdAndShopId(orderId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private void addHistory(Order order, Order.OrderStatus status, String description) {
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(status).description(description).build());
    }

    private Order.OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private ShopOrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .toList();

        Address a = order.getDeliveryAddress();
        AddressResponse addr = a == null ? null : AddressResponse.builder()
                .id(a.getId()).label(a.getLabel()).recipientName(a.getRecipientName())
                .phone(a.getPhone()).line1(a.getLine1()).line2(a.getLine2())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode())
                .country(a.getCountry()).isDefault(a.getIsDefault()).build();

        return ShopOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge())
                .totalAmount(order.getTotalAmount())
                .customerName(order.getUser() != null ? order.getUser().getName() : null)
                .customerPhone(order.getUser() != null ? order.getUser().getPhone() : null)
                .deliveryAddress(addr)
                .items(items)
                .deliveryPartner(order.getDeliveryPartner() != null ? order.getDeliveryPartner().name() : null)
                .deliveryTrackingId(order.getDeliveryTrackingId())
                .deliveryStatus(order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : null)
                .estimatedDelivery(order.getEstimatedDelivery())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
