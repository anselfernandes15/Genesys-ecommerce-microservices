package com.genesys.order.client;

import com.genesys.order.config.JwtUtil;
import com.genesys.order.model.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    private final JwtUtil jwtUtil;

    public ProductServiceClient(RestTemplate restTemplate,
                                @Value("${product.service.url}") String productServiceUrl,
                                JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse getProductById(Long productId) {
        log.info("Calling Product Service for product ID: {}", productId);
        String url = productServiceUrl + "/products/" + productId;

        // Generate service token for inter-service authentication
        String token = jwtUtil.generateToken("order-service");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, ProductResponse.class).getBody();
    }

    public ProductResponse getProductFallback(Long productId, Throwable throwable) {
        log.warn("Product Service is unavailable. Circuit breaker fallback triggered for product ID: {}. Error: {}",
                productId, throwable.getMessage());
        return null;
    }
}