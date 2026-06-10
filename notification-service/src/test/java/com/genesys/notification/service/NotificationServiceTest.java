package com.genesys.notification.service;

import com.genesys.notification.model.dto.NotificationRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void sendOrderConfirmation_Success() {
        NotificationRequest request = new NotificationRequest();
        request.setOrderId(1L);
        request.setCustomerEmail("ansel@test.com");
        request.setCustomerName("Ansel Fernandes");
        request.setProductName("Wireless Headphones");
        request.setQuantity(2);
        request.setTotalPrice(new BigDecimal("159.98"));

        assertDoesNotThrow(() -> notificationService.sendOrderConfirmation(request));
    }

    @Test
    void sendOrderConfirmation_NullFields() {
        NotificationRequest request = new NotificationRequest();
        request.setOrderId(2L);

        assertDoesNotThrow(() -> notificationService.sendOrderConfirmation(request));
    }
}