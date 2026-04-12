package com.pawnavz.service;

import com.pawnavz.dto.request.PaymentRequest;
import com.pawnavz.dto.response.PaymentResponse;
import com.pawnavz.entity.*;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.OrderRepository;
import com.pawnavz.repository.PaymentRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @Transactional
    public PaymentResponse initiatePayment(String userId, PaymentRequest req) {
        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", req.getOrderId()));
        guardPayable(order);

        paymentRepository.findByOrderId(order.getId()).ifPresent(existing -> {
            if (existing.getStatus() == Order.PaymentStatus.PAID)
                throw new BadRequestException("Order is already paid");
            paymentRepository.delete(existing);
        });

        Payment payment = paymentRepository.save(Payment.builder()
                .order(order).user(userRepository.getReferenceById(userId))
                .amount(order.getTotalAmount()).method(req.getMethod())
                .status(Order.PaymentStatus.PENDING).build());

        return req.getMethod() == Order.PaymentMethod.COD
                ? processCod(payment, order)
                : routeGateway(payment, order, req);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String userId, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for order: " + orderId));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse retryPayment(String userId, PaymentRequest req) {
        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", req.getOrderId()));
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BadRequestException("No existing payment to retry"));
        if (payment.getStatus() == Order.PaymentStatus.PAID)
            throw new BadRequestException("Payment already complete");
        if (payment.getStatus() != Order.PaymentStatus.FAILED)
            throw new BadRequestException("Only failed payments can be retried");
        payment.setMethod(req.getMethod());
        payment.setStatus(Order.PaymentStatus.PENDING);
        payment.setGatewayReference(null);
        payment.setGatewayResponse(null);
        payment.setFailureReason(null);
        return routeGateway(paymentRepository.save(payment), order, req);
    }

    private PaymentResponse routeGateway(Payment payment, Order order, PaymentRequest req) {
        return switch (req.getMethod()) {
            case CARD -> processCard(payment, order, req);
            case UPI -> processUpi(payment, order, req);
            case NET_BANKING -> processNetBanking(payment, order, req);
            case WALLET -> processWallet(payment, order, req);
            default -> throw new BadRequestException("Unsupported payment method");
        };
    }

    private PaymentResponse processCod(Payment payment, Order order) {
        payment.setGatewayReference("COD-" + ref());
        payment.setGatewayResponse(gwJson("COD_CONFIRMED", "Cash on delivery confirmed"));
        return applySuccess(payment, order, Order.OrderStatus.CONFIRMED, "Order confirmed. Pay on delivery.");
    }

    private PaymentResponse processCard(Payment payment, Order order, PaymentRequest req) {
        if ("4000000000000002".equals(req.getCardNumber()))
            return applyFailure(payment, order, "Card declined by issuer");
        if ("4000000000009995".equals(req.getCardNumber()))
            return applyFailure(payment, order, "Insufficient funds");
        payment.setGatewayReference("CARD-" + ref());
        payment.setGatewayResponse(gwJson("CHARGE_SUCCESS", "Card authorized and captured"));
        return applySuccess(payment, order, Order.OrderStatus.CONFIRMED, "Payment successful");
    }

    private PaymentResponse processUpi(Payment payment, Order order, PaymentRequest req) {
        if (req.getUpiId() == null || !req.getUpiId().contains("@"))
            return applyFailure(payment, order, "Invalid UPI ID");
        if ("fail@upi".equalsIgnoreCase(req.getUpiId()))
            return applyFailure(payment, order, "UPI transaction declined");
        payment.setGatewayReference("UPI-" + ref());
        payment.setGatewayResponse(gwJson("UPI_SUCCESS", "UPI payment collected from " + req.getUpiId()));
        return applySuccess(payment, order, Order.OrderStatus.CONFIRMED, "UPI payment successful");
    }

    private PaymentResponse processNetBanking(Payment payment, Order order, PaymentRequest req) {
        if (req.getBankCode() == null || req.getBankCode().isBlank())
            return applyFailure(payment, order, "Bank code required for net banking");
        payment.setGatewayReference("NB-" + ref());
        payment.setGatewayResponse(gwJson("NB_SUCCESS", "Net banking payment received"));
        return applySuccess(payment, order, Order.OrderStatus.CONFIRMED, "Net banking payment successful");
    }

    private PaymentResponse processWallet(Payment payment, Order order, PaymentRequest req) {
        if (req.getWalletProvider() == null || req.getWalletProvider().isBlank())
            return applyFailure(payment, order, "Wallet provider required");
        payment.setGatewayReference("WLT-" + ref());
        payment.setGatewayResponse(gwJson("WALLET_SUCCESS", "Wallet debited from " + req.getWalletProvider()));
        return applySuccess(payment, order, Order.OrderStatus.CONFIRMED, "Wallet payment successful");
    }

    private PaymentResponse applySuccess(Payment payment, Order order,
                                          Order.OrderStatus newStatus, String desc) {
        payment.setStatus(Order.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(newStatus);
        order.setPaymentReference(payment.getGatewayReference());
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order).status(newStatus).description(desc)
                .changedBy("payment-service").build());
        orderRepository.save(order);

        notificationService.createNotification(order.getUser().getId(),
                Notification.NotificationType.PAYMENT, "Payment Successful",
                "₹" + payment.getAmount() + " paid for order #" + order.getOrderNumber(),
                order.getId(), "ORDER");

        log.info("[PAYMENT] SUCCESS order={} ref={}", order.getOrderNumber(), payment.getGatewayReference());
        return mapToResponse(saved);
    }

    private PaymentResponse applyFailure(Payment payment, Order order, String reason) {
        payment.setStatus(Order.PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setGatewayResponse(gwJson("PAYMENT_FAILED", reason));
        Payment saved = paymentRepository.save(payment);
        order.setPaymentStatus(Order.PaymentStatus.FAILED);
        orderRepository.save(order);

        notificationService.createNotification(order.getUser().getId(),
                Notification.NotificationType.PAYMENT, "Payment Failed",
                "Payment for order #" + order.getOrderNumber() + " failed: " + reason,
                order.getId(), "ORDER");

        log.warn("[PAYMENT] FAILED order={} reason={}", order.getOrderNumber(), reason);
        return mapToResponse(saved);
    }

    private void guardPayable(Order order) {
        if (order.getStatus() == Order.OrderStatus.CANCELLED)
            throw new BadRequestException("Cannot pay for a cancelled order");
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID)
            throw new BadRequestException("Order is already paid");
    }

    private String ref() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String gwJson(String code, String message) {
        return "{\"code\":\"" + code + "\",\"message\":\"" + message
                + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getId()).orderId(p.getOrder().getId())
                .orderNumber(p.getOrder().getOrderNumber()).amount(p.getAmount())
                .method(p.getMethod().name()).status(p.getStatus().name())
                .gatewayReference(p.getGatewayReference()).failureReason(p.getFailureReason())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt()).build();
    }
}
