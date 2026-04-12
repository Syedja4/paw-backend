package com.pawnavz.controller;

import com.pawnavz.dto.request.PaymentRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.PaymentResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiate(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment initiated",
                paymentService.initiatePayment(uid(auth), request)));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment status for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> getStatus(
            @RequestHeader("Authorization") String auth,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentStatus(uid(auth), orderId)));
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry a failed payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> retry(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment retried",
                paymentService.retryPayment(uid(auth), request)));
    }
}
