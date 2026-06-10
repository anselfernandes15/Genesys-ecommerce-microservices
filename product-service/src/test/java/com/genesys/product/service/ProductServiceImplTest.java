package com.genesys.product.service;

import com.genesys.product.exception.ResourceNotFoundException;
import com.genesys.product.mapper.ProductMapper;
import com.genesys.product.model.dto.ProductRequest;
import com.genesys.product.model.dto.ProductResponse;
import com.genesys.product.model.entity.Product;
import com.genesys.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Headphones");
        product.setPrice(new BigDecimal("79.99"));
        product.setStockQuantity(150);
        product.setSku("WH-1000");
        product.setCategory("Electronics");
        product.setActive(true);

        productRequest = new ProductRequest();
        productRequest.setName("Wireless Headphones");
        productRequest.setPrice(new BigDecimal("79.99"));
        productRequest.setStockQuantity(150);
        productRequest.setSku("WH-1000");
        productRequest.setCategory("Electronics");

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Wireless Headphones");
        productResponse.setPrice(new BigDecimal("79.99"));
        productResponse.setStockQuantity(150);
    }

    @Test
    void createProduct_Success() {
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(productRequest);

        assertNotNull(result);
        assertEquals("Wireless Headphones", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1L);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}