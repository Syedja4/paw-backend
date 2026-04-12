package com.pawnavz.service.driver;

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

@Service
@RequiredArgsConstructor
public class DriverOrderService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;

    public Page<OrderResponse> getMyOrders(String status, Pageable pageable) {
        Driver driver = driverService.getCurrentDriver();
        return orderRepository.findWithFilters(parseStatus(status), null, driver.getId(), null, null, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse markPickedUp(String orderId) {
        Driver driver = driverService.getCurrentDriver();
        Order order = findAndValidate(orderId, driver);
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new BadRequestException("Order must be SHIPPED status to mark as picked up");
        }
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markDelivered(String orderId) {
        Driver driver = driverService.getCurrentDriver();
        Order order = findAndValidate(orderId, driver);
        if (order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Order must be OUT_FOR_DELIVERY to mark as delivered");
        }
        order.setStatus(Order.OrderStatus.DELIVERED);
        BigDecimal commission = order.getTotalAmount().multiply(BigDecimal.valueOf(0.10));
        driver.setTotalEarnings(driver.getTotalEarnings().add(commission));
        driver.setAvailable(true);
        driverRepository.save(driver);
        return mapToResponse(orderRepository.save(order));
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

    private Order findAndValidate(String orderId, Driver driver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new BadRequestException("This order is not assigned to you");
        }
        return order;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .estimatedDelivery(order.getEstimatedDelivery())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .driverId(order.getDriver() != null ? String.valueOf(order.getDriver().getId()) : null)
                .driverName(order.getDriver() != null ? order.getDriver().getUser().getName() : null)
                .driverVehicleNumber(order.getDriver() != null ? order.getDriver().getVehicleNumber() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
