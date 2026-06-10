package com.genesys.order.service;

import com.genesys.order.client.NotificationServiceClient;  // NEW
import com.genesys.order.client.ProductServiceClient;
import com.genesys.order.exception.ResourceNotFoundException;
import com.genesys.order.mapper.OrderMapper;
import com.genesys.order.model.dto.OrderRequest;
import com.genesys.order.model.dto.OrderResponse;
import com.genesys.order.model.dto.ProductResponse;
import com.genesys.order.model.entity.Order;
import com.genesys.order.model.enums.OrderStatus;
import com.genesys.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductServiceClient productServiceClient;
    private final NotificationServiceClient notificationServiceClient;


    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            ProductServiceClient productServiceClient,
                            NotificationServiceClient notificationServiceClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productServiceClient = productServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing order for product ID: {}", request.getProductId());
        ProductResponse product = productServiceClient.getProductById(request.getProductId());

        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.getStockQuantity()
                            + ", Requested: " + request.getQuantity());
        }

        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setProductName(product.getName());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(product.getPrice());
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerName(request.getCustomerName());

        Order saved = orderRepository.save(order);
        log.info("Order placed successfully with ID: {}", saved.getId());

//        log.info("DEBUG: About to send notification to notification-service");

        notificationServiceClient.sendOrderNotification(
                saved.getId(),
                saved.getCustomerEmail(),
                saved.getCustomerName(),
                saved.getProductName(),
                saved.getQuantity(),
                saved.getTotalPrice()
        );

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
}