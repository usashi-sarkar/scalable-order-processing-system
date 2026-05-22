package com.interview.ordersystem.inventory;

import com.interview.ordersystem.exception.BadRequestException;
import com.interview.ordersystem.exception.ResourceNotFoundException;
import com.interview.ordersystem.product.Product;
import com.interview.ordersystem.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductService productService;

    public InventoryResponse upsert(InventoryRequest request) {
        Product product = productService.getEntity(request.getProductId());
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElse(Inventory.builder().product(product).availableQuantity(0).build());
        inventory.setAvailableQuantity(request.getQuantity());
        return toResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse findByProduct(Long productId) {
        return toResponse(inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product " + productId)));
    }

    @Transactional
    public void reserve(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product " + productId));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for product " + productId);
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    public InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .availableQuantity(inventory.getAvailableQuantity())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
