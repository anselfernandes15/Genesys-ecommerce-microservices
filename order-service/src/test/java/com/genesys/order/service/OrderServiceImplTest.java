package com.genesys.order.service;

import com.genesys.order.client.NotificationServiceClient;
import com.genesys.order.client.ProductServiceClient;
import com.genesys.order.exception.ResourceNotFoundException;
import com.genesys.order.mapper.OrderMapper;
import com.genesys.order.model.dto.OrderRequest;
import com.genesys.order.model.dto.OrderResponse;
import com.genesys.order.model.dto.ProductResponse;
import com.genesys.order.model.entity.Order;
import com.genesys.order.model.enums.OrderStatus;
import com.genesys.order.repository.OrderRepository;
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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private ProductResponse productResponse;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setProductId(1L);
        orderRequest.setQuantity(2);
        orderRequest.setCustomerEmail("ansel@test.com");
        orderRequest.setCustomerName("Ansel Fernandes");

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Wireless Headphones");
        productResponse.setPrice(new BigDecimal("79.99"));
        productResponse.setStockQuantity(150);
        productResponse.setActive(true);

        order = new Order();
        order.setId(1L);
        order.setProductId(1L);
        order.setProductName("Wireless Headphones");
        order.setQuantity(2);
        order.setUnitPrice(new BigDecimal("79.99"));
        order.setTotalPrice(new BigDecimal("159.98"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerEmail("ansel@test.com");
        order.setCustomerName("Ansel Fernandes");

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);
        orderResponse.setProductId(1L);
        orderResponse.setProductName("Wireless Headphones");
        orderResponse.setQuantity(2);
        orderResponse.setTotalPrice(new BigDecimal("159.98"));
        orderResponse.setStatus(OrderStatus.CONFIRMED);
    }

    @Test
    void placeOrder_Success() {
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.placeOrder(orderRequest);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(new BigDecimal("159.98"), result.getTotalPrice());
        verify(productServiceClient).getProductById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(notificationServiceClient).sendOrderNotification(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void placeOrder_ProductNotFound() {
        when(productServiceClient.getProductById(999L)).thenReturn(null);
        orderRequest.setProductId(999L);

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.placeOrder(orderRequest);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_InsufficientStock() {
        productResponse.setStockQuantity(1);
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        orderRequest.setQuantity(10);

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(orderRequest);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }
}