package com.pawnavz.service;

import com.pawnavz.dto.request.CreateOrderRequest;
import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.entity.*;
import com.pawnavz.exception.*;
import com.pawnavz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", request.getAddressId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            if (p.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("'" + p.getName() + "' has insufficient stock");
            }
            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        BigDecimal delivery = subtotal.compareTo(new BigDecimal("499")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("49");
        BigDecimal total = subtotal.add(delivery);

        Order order = Order.builder()
                .orderNumber("PNZ-" + System.currentTimeMillis() + "-"
                        + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                .user(userRepository.getReferenceById(userId))
                .deliveryAddress(address)
                .subtotal(subtotal).deliveryCharge(delivery).totalAmount(total)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .status(Order.OrderStatus.PENDING)
                .estimatedDelivery(LocalDateTime.now().plusDays(3))
                .notes(request.getNotes())
                .build();

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product p = cartItem.getProduct();
            p.setStockQuantity(p.getStockQuantity() - cartItem.getQuantity());
            p.setOrderCount(p.getOrderCount() + 1);
            productRepository.save(p);
            return OrderItem.builder()
                    .order(order).product(p)
                    .productName(p.getName())
                    .productImage(p.getImageUrls().isEmpty() ? null : p.getImageUrls().get(0))
                    .unitPrice(p.getPrice()).quantity(cartItem.getQuantity())
                    .totalPrice(p.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .returnStatus(OrderItem.ReturnStatus.NONE).build();
        }).toList();

        order.getItems().addAll(orderItems);
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(Order.OrderStatus.PENDING)
                .description("Order placed successfully").build());

        Order saved = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        notificationService.createNotification(userId,
                Notification.NotificationType.ORDER_UPDATE,
                "Order Placed!",
                "Your order #" + saved.getOrderNumber() + " has been placed.",
                saved.getId(), "ORDER");

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(String userId, String orderId) {
        return mapToResponse(orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId)));
    }

    @Transactional
    public OrderResponse cancelOrder(String userId, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != Order.OrderStatus.PENDING
                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getStatus());
        }
        order.getItems().forEach(item -> {
            item.getProduct().setStockQuantity(
                    item.getProduct().getStockQuantity() + item.getQuantity());
            productRepository.save(item.getProduct());
        });
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(Order.OrderStatus.CANCELLED)
                .description("Order cancelled by user").changedBy(userId).build());
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream().map(i ->
                OrderResponse.OrderItemResponse.builder()
                        .id(i.getId()).productId(i.getProduct().getId())
                        .productName(i.getProductName()).productImage(i.getProductImage())
                        .unitPrice(i.getUnitPrice()).quantity(i.getQuantity())
                        .totalPrice(i.getTotalPrice())
                        .returnStatus(i.getReturnStatus() != null ? i.getReturnStatus().name() : null)
                        .build()).toList();

        List<OrderResponse.StatusHistoryResponse> timeline = order.getStatusHistory().stream()
                .map(h -> OrderResponse.StatusHistoryResponse.builder()
                        .status(h.getStatus().name()).description(h.getDescription())
                        .changedAt(h.getChangedAt()).build()).toList();

        Address a = order.getDeliveryAddress();
        AddressResponse addr = a == null ? null : AddressResponse.builder()
                .id(a.getId()).label(a.getLabel()).recipientName(a.getRecipientName())
                .phone(a.getPhone()).line1(a.getLine1()).line2(a.getLine2())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode())
                .country(a.getCountry()).isDefault(a.getIsDefault()).build();

        return OrderResponse.builder()
                .id(order.getId()).orderNumber(order.getOrderNumber())
                .status(order.getStatus().name()).paymentStatus(order.getPaymentStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .subtotal(order.getSubtotal()).deliveryCharge(order.getDeliveryCharge())
                .discountAmount(order.getDiscountAmount()).totalAmount(order.getTotalAmount())
                .deliveryAddress(addr).items(items).timeline(timeline)
                .estimatedDelivery(order.getEstimatedDelivery()).createdAt(order.getCreatedAt())
                .build();
    }
}
