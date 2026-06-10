package com.genesys.order.service;

import com.genesys.order.model.dto.OrderRequest;
import com.genesys.order.model.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();
}