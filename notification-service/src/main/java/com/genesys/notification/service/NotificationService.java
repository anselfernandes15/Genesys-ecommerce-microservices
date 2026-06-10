package com.genesys.notification.service;

import com.genesys.notification.model.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendOrderConfirmation(NotificationRequest request) {
        // In production: send email via SES, SendGrid, etc.
        // For this demo: log the notification
        log.info("========== ORDER NOTIFICATION ==========");
        log.info("To: {} ({})", request.getCustomerName(), request.getCustomerEmail());
        log.info("Order ID: {}", request.getOrderId());
        log.info("Product: {} x{}", request.getProductName(), request.getQuantity());
        log.info("Total: ${}", request.getTotalPrice());
        log.info("Status: CONFIRMED");
        log.info("=========================================");
    }
}