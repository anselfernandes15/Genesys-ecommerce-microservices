package com.genesys.product.mapper;

import com.genesys.product.model.dto.ProductRequest;
import com.genesys.product.model.dto.ProductResponse;
import com.genesys.product.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // Request DTO → Entity (used when creating a new product)
    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setActive(true);
        return product;
    }

    // Entity → Response DTO (used when returning data to client)
    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSku(product.getSku());
        response.setCategory(product.getCategory());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    // Request DTO → existing Entity (used when updating a product)
    public void updateEntity(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
    }
}