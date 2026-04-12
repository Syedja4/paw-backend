package com.pawnavz.service.admin;

import com.pawnavz.dto.request.AssignDriverRequest;
import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.dto.response.OrderResponse;
import com.pawnavz.entity.Driver;
import com.pawnavz.entity.Order;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.DriverRepository;
import com.pawnavz.repository.OrderRepository;
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
    private final DriverRepository driverRepository;

    public Page<OrderResponse> getAllOrders(String status, String userId, String driverId,
                                            LocalDateTime from, LocalDateTime to,
                                            Pageable pageable) {
        System.out.println("ADMIN API HIT");
        try {
            Long parsedDriverId = parseDriverId(driverId);
            Order.OrderStatus parsedStatus = parseStatus(status);
            Page<Order> orders = orderRepository.findWithFilters(parsedStatus, userId, parsedDriverId, from, to, pageable);
            return orders != null ? orders.map(this::mapToResponse) : Page.empty(pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    public OrderResponse getOrderById(String id) {
        System.out.println("ADMIN API HIT");
        try {
            return mapToResponse(findById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return emptyOrderResponse();
        }
    }

    @Transactional
    public OrderResponse assignDriver(String orderId, AssignDriverRequest request) {
        System.out.println("ADMIN API HIT");
        try {
            Order order = findById(orderId);

            Long parsedDriverId;
            try {
                parsedDriverId = Long.valueOf(request.getDriverId());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid driver id: " + request.getDriverId());
            }

            Driver driver = driverRepository.findById(parsedDriverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));

            if (!driver.isAvailable()) {
                throw new BadRequestException("Driver is not available for assignment");
            }

            order.setDriver(driver);
            order.setStatus(Order.OrderStatus.SHIPPED);
            driver.setAvailable(false);
            driverRepository.save(driver);
            return mapToResponse(orderRepository.save(order));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String statusStr) {
        System.out.println("ADMIN API HIT");
        try {
            Order order = findById(orderId);
            Order.OrderStatus status;
            try {
                status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + statusStr +
                        ". Valid values: PENDING, CONFIRMED, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED");
            }
            order.setStatus(status);
            if (status == Order.OrderStatus.DELIVERED || status == Order.OrderStatus.CANCELLED) {
                if (order.getDriver() != null) {
                    Driver driver = order.getDriver();
                    driver.setAvailable(true);
                    driverRepository.save(driver);
                }
            }
            return mapToResponse(orderRepository.save(order));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Order findById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private Long parseDriverId(String driverId) {
        if (driverId == null || driverId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(driverId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid driver id: " + driverId);
        }
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
        Driver driver = order.getDriver();
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
                .driverId(driver != null ? String.valueOf(driver.getId()) : "")
                .driverName(driver != null && driver.getUser() != null ? Optional.ofNullable(driver.getUser().getName()).orElse("") : "")
                .driverPhone(driver != null && driver.getUser() != null ? Optional.ofNullable(driver.getUser().getPhone()).orElse("") : "")
                .driverVehicleNumber(driver != null ? Optional.ofNullable(driver.getVehicleNumber()).orElse("") : "")
                .createdAt(Optional.ofNullable(order.getCreatedAt()).orElse(LocalDateTime.now()))
                .updatedAt(Optional.ofNullable(order.getUpdatedAt()).orElse(LocalDateTime.now()))
                .build();
    }

    private AddressResponse emptyAddress() {
        return AddressResponse.builder()
                .id("")
                .label("")
                .recipientName("")
                .phone("")
                .line1("")
                .line2("")
                .city("")
                .state("")
                .pincode("")
                .country("")
                .isDefault(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private OrderResponse emptyOrderResponse() {
        return OrderResponse.builder()
                .id("")
                .orderNumber("")
                .status("")
                .paymentStatus("")
                .paymentMethod("")
                .subtotal(BigDecimal.ZERO)
                .deliveryCharge(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .deliveryAddress(emptyAddress())
                .items(Collections.emptyList())
                .timeline(Collections.emptyList())
                .estimatedDelivery(LocalDateTime.now())
                .userId("")
                .userName("Unknown")
                .userPhone("")
                .driverId("")
                .driverName("")
                .driverPhone("")
                .driverVehicleNumber("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
