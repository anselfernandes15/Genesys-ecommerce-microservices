package com.genesys.order.client;

import com.genesys.order.model.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                                @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse getProductById(Long productId) {
        log.info("Calling Product Service for product ID: {}", productId);
        String url = productServiceUrl + "/products/" + productId;
        return restTemplate.getForObject(url, ProductResponse.class);
    }

    // Fallback — called when Product Service is down or circuit is open
    public ProductResponse getProductFallback(Long productId, Throwable throwable) {
        log.warn("Product Service is unavailable. Circuit breaker fallback triggered for product ID: {}. Error: {}",
                productId, throwable.getMessage());
        return null;  // OrderServiceImpl checks for null and throws ResourceNotFoundException
    }
}