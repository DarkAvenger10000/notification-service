package com.kiran.notification_service.controller;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received notification request for recipient: {}", request.getRecipient());
        try {
            notificationService.sendNotification(request);
            return ResponseEntity.ok("Notification queued successfully!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Rate limit exceeded")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Rate limit exceeded. Max 5 notifications per hour per recipient.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process notification.");
        }
    }
}
