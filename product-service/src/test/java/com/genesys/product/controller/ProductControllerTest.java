package com.genesys.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesys.product.config.JwtUtil;
import com.genesys.product.model.dto.ProductRequest;
import com.genesys.product.model.dto.ProductResponse;
import com.genesys.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private String token;

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateToken("admin");

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
        productResponse.setActive(true);
    }

    @Test
    void createProduct_WithToken_Returns201() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Headphones"));
    }

    @Test
    void getAllProducts_WithToken_Returns200() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getProductById_WithToken_Returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/products/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wireless Headphones"));
    }

    @Test
    void getProducts_WithoutToken_Returns403() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getToken_WithoutAuth_IsAccessible() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
    }
}