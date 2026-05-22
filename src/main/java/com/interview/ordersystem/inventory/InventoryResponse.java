package com.interview.ordersystem.inventory;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InventoryResponse {
    private Long productId;
    private String productName;
    private int availableQuantity;
    private Instant updatedAt;
}
