package com.interview.ordersystem.product;

import com.interview.ordersystem.exception.BadRequestException;
import com.interview.ordersystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists");
        }
        Product saved = productRepository.save(Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build());
        return toResponse(saved);
    }

    @Cacheable(value = "products")
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        return toResponse(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deactivate(Long id) {
        Product product = getEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .active(product.isActive())
                .build();
    }
}
