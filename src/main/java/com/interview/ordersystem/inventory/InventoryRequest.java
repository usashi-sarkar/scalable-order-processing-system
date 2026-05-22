package com.interview.ordersystem.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {
    @NotNull
    private Long productId;

    @Min(0)
    private int quantity;
}
