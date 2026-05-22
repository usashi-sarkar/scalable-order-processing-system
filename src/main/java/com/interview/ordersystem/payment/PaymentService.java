package com.interview.ordersystem.payment;

import com.interview.ordersystem.notification.NotificationService;
import com.interview.ordersystem.order.CustomerOrder;
import com.interview.ordersystem.order.OrderRepository;
import com.interview.ordersystem.order.OrderService;
import com.interview.ordersystem.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse processPayment(Long orderId) {
        var existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        CustomerOrder order = orderService.getEntity(orderId);

        PaymentStatus status = order.getTotalAmount().remainder(java.math.BigDecimal.valueOf(2)).intValue() == 0
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(status)
                .transactionReference("SIM-" + UUID.randomUUID())
                .paidAt(Instant.now())
                .build();

        order.setStatus(status == PaymentStatus.SUCCESS ? OrderStatus.CONFIRMED : OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        Payment saved = paymentRepository.save(payment);

        notificationService.create(order.getUser().getId(),
                status == PaymentStatus.SUCCESS
                        ? "Order " + orderId + " confirmed"
                        : "Payment failed for order " + orderId);

        return toResponse(saved);
    }

    public PaymentResponse findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).map(this::toResponse)
                .orElseThrow(() -> new com.interview.ordersystem.exception.ResourceNotFoundException("Payment not found"));
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
