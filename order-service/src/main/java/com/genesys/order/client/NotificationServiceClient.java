package com.genesys.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationServiceClient(RestTemplate restTemplate,
                                     @Value("${notification.service.url}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void sendOrderNotification(Long orderId, String customerEmail, String customerName,
                                      String productName, Integer quantity, BigDecimal totalPrice) {
//        log.info("DEBUG: sendOrderNotification called for order {}", orderId);
        try {
            String url = notificationServiceUrl + "/notifications";

            Map<String, Object> request = new HashMap<>();
            request.put("order_id", orderId);
            request.put("customer_email", customerEmail);
            request.put("customer_name", customerName);
            request.put("product_name", productName);
            request.put("quantity", quantity);
            request.put("total_price", totalPrice);

            restTemplate.postForObject(url, request, String.class);
            log.info("Notification sent for order ID: {}", orderId);
        } catch (Exception e) {
            // Don't fail the order if notification fails
            log.error("Failed to send notification for order ID: {}", orderId, e);
        }
    }
}