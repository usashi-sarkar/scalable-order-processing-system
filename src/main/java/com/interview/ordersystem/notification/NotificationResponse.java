package com.interview.ordersystem.notification;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String message;
    private Instant createdAt;
}
