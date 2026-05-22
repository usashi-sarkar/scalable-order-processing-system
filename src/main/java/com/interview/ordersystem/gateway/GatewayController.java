package com.interview.ordersystem.gateway;

import com.interview.ordersystem.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> routes() {
        List<Map<String, String>> routes = List.of(
                Map.of("module", "Authentication", "path", "/api/auth/**"),
                Map.of("module", "Products", "path", "/api/products/**"),
                Map.of("module", "Inventory", "path", "/api/inventory/**"),
                Map.of("module", "Orders", "path", "/api/orders/**"),
                Map.of("module", "Payments", "path", "/api/payments/**"),
                Map.of("module", "Notifications", "path", "/api/notifications/**")
        );
        return ResponseEntity.ok(ApiResponse.ok("Gateway route registry", routes));
    }
}
