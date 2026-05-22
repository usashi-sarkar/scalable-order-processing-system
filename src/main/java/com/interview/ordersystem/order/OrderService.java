package com.interview.ordersystem.order;

import com.interview.ordersystem.exception.ResourceNotFoundException;
import com.interview.ordersystem.inventory.InventoryService;
import com.interview.ordersystem.messaging.KafkaProducerService;
import com.interview.ordersystem.product.Product;
import com.interview.ordersystem.product.ProductService;
import com.interview.ordersystem.user.User;
import com.interview.ordersystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public OrderResponse create(CreateOrderRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CustomerOrder order = CustomerOrder.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.getEntity(itemRequest.getProductId());
            inventoryService.reserve(product.getId(), itemRequest.getQuantity());

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.getItems().add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(total);
        CustomerOrder saved = orderRepository.save(order);

        kafkaProducerService.publishOrderCreated(new OrderEvent(saved.getId(), user.getId(), saved.getTotalAmount()));
        return toResponse(saved);
    }

    public List<OrderResponse> myOrders(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    public OrderResponse findById(Long id) {
        return toResponse(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id)));
    }

    public CustomerOrder getEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    public OrderResponse toResponse(CustomerOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build()).toList())
                .build();
    }
}
