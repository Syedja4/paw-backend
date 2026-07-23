package com.pawnavz.service.admin;

import com.pawnavz.delivery.DeliveryDispatchResult;
import com.pawnavz.delivery.DeliveryService;
import com.pawnavz.dto.request.AssignShopRequest;
import com.pawnavz.dto.request.RequestDeliveryRequest;
import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.entity.Order;
import com.pawnavz.entity.OrderStatusHistory;
import com.pawnavz.entity.Shop;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.OrderRepository;
import com.pawnavz.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final DeliveryService deliveryService;

    public Page<OrderResponse> getAllOrders(String status, String userId, String shopId,
                                            LocalDateTime from, LocalDateTime to,
                                            Pageable pageable) {
        String normalizedShopId = (shopId == null || shopId.isBlank()) ? null : shopId;
        Order.OrderStatus parsedStatus = parseStatus(status);
        String normalizedUserId = (userId == null || userId.isBlank()) ? null : userId;
        Page<Order> orders = orderRepository.findWithFilters(
                parsedStatus, normalizedUserId, normalizedShopId, from, to, pageable);
        return orders.map(this::mapToResponse);
    }

    public OrderResponse getOrderById(String id) {
        return mapToResponse(findById(id));
    }

    /**
     * Manual override to (re)assign an order to a specific shop. Orders are normally
     * auto-routed to a shop by delivery-address PIN at creation time
     * (see OrderService + ServiceAvailabilityService); this endpoint only overrides that.
     */
    @Transactional
    public OrderResponse assignShop(String orderId, AssignShopRequest request) {
        Order order = findById(orderId);
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop", request.getShopId()));
        if (shop.getStatus() != Shop.ShopStatus.ACTIVE) {
            throw new BadRequestException("Shop is not ACTIVE and cannot receive orders");
        }
        order.setShop(shop);
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(order.getStatus())
                .description("Order routed to shop: " + shop.getShopName()).build());
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * Requests a delivery from a third-party logistics partner (Porter/Ola).
     * The actual provider call is delegated to {@link DeliveryService}, so wiring a
     * real integration requires no change to this method.
     */
    @Transactional
    public OrderResponse requestDelivery(String orderId, RequestDeliveryRequest request) {
        Order order = findById(orderId);
        if (order.getShop() == null) {
            throw new BadRequestException("Assign the order to a shop before requesting delivery");
        }
        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new BadRequestException("Order must be PROCESSING (ready) before requesting delivery");
        }

        DeliveryDispatchResult result = (request != null && request.getPartner() != null)
                ? deliveryService.requestDelivery(order, request.getPartner())
                : deliveryService.requestDelivery(order);

        order.setDeliveryPartner(result.getPartner());
        order.setDeliveryTrackingId(result.getTrackingId());
        order.setDeliveryStatus(result.getStatus());
        order.setStatus(Order.OrderStatus.SHIPPED);
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(Order.OrderStatus.SHIPPED)
                .description("Delivery requested via " + result.getPartner()
                        + " (tracking: " + result.getTrackingId() + ")").build());
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String statusStr) {
        Order order = findById(orderId);
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusStr +
                    ". Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED");
        }
        order.setStatus(status);
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(status).description("Status updated by admin").build());
        return mapToResponse(orderRepository.save(order));
    }

    private Order findById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private Order.OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        Shop shop = order.getShop();
        return OrderResponse.builder()
                .id(Optional.ofNullable(order.getId()).orElse(""))
                .orderNumber(Optional.ofNullable(order.getOrderNumber()).orElse(""))
                .status(order.getStatus() != null ? order.getStatus().name() : "")
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "")
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "")
                .subtotal(Optional.ofNullable(order.getSubtotal()).orElse(BigDecimal.ZERO))
                .deliveryCharge(Optional.ofNullable(order.getDeliveryCharge()).orElse(BigDecimal.ZERO))
                .discountAmount(Optional.ofNullable(order.getDiscountAmount()).orElse(BigDecimal.ZERO))
                .totalAmount(Optional.ofNullable(order.getTotalAmount()).orElse(BigDecimal.ZERO))
                .deliveryAddress(emptyAddress())
                .items(Collections.emptyList())
                .timeline(Collections.emptyList())
                .estimatedDelivery(Optional.ofNullable(order.getEstimatedDelivery()).orElse(LocalDateTime.now()))
                .userId(order.getUser() != null ? Optional.ofNullable(order.getUser().getId()).orElse("") : "")
                .userName(order.getUser() != null ? Optional.ofNullable(order.getUser().getName()).orElse("Unknown") : "Unknown")
                .userPhone(order.getUser() != null ? Optional.ofNullable(order.getUser().getPhone()).orElse("") : "")
                .shopId(shop != null ? Optional.ofNullable(shop.getId()).orElse("") : "")
                .shopName(shop != null ? Optional.ofNullable(shop.getShopName()).orElse("") : "")
                .shopPhone(shop != null ? Optional.ofNullable(shop.getPhone()).orElse("") : "")
                .deliveryPartner(order.getDeliveryPartner() != null ? order.getDeliveryPartner().name() : "")
                .deliveryTrackingId(Optional.ofNullable(order.getDeliveryTrackingId()).orElse(""))
                .deliveryStatus(order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : "")
                .createdAt(Optional.ofNullable(order.getCreatedAt()).orElse(LocalDateTime.now()))
                .updatedAt(Optional.ofNullable(order.getUpdatedAt()).orElse(LocalDateTime.now()))
                .build();
    }

    private AddressResponse emptyAddress() {
        return AddressResponse.builder()
                .id("").label("").recipientName("").phone("").line1("").line2("")
                .city("").state("").pincode("").country("")
                .isDefault(Boolean.FALSE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }
}
