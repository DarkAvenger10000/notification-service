package com.kiran.notification_service.kafka;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @KafkaListener(topics = "notification.email", groupId = "notification-group")
    public void consumeEmailNotification(String message) {
        log.info("Received message from Kafka: {}", message);
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            emailService.sendEmail(request);
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage());
        }
    }
}
