package com.interview.ordersystem.notification;

import com.interview.ordersystem.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched", notificationService.byUser(userId)));
    }
}
