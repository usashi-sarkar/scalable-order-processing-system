package com.interview.ordersystem.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.ordersystem.order.OrderEvent;
import com.interview.ordersystem.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.order-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) throws JsonProcessingException {
        OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
        log.info("Received order event: {}", event);
        paymentService.processPayment(event.getOrderId());
    }
}
