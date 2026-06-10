package com.genesys.notification.controller;

import com.genesys.notification.model.dto.NotificationRequest;
import com.genesys.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendOrderConfirmation(request);
        return ResponseEntity.ok("Notification sent successfully");
    }
}