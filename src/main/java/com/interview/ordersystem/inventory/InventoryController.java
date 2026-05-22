package com.interview.ordersystem.inventory;

import com.interview.ordersystem.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> upsert(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory updated", inventoryService.upsert(request)));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> byProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory fetched", inventoryService.findByProduct(productId)));
    }
}
