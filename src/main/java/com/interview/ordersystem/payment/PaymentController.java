package com.interview.ordersystem.payment;

import com.interview.ordersystem.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/simulate/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> simulate(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Payment simulated", paymentService.processPayment(orderId)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> byOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Payment fetched", paymentService.findByOrderId(orderId)));
    }
}
