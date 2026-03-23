package com.kiran.notification_service.kafka;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.service.EmailService;
import com.kiran.notification_service.service.SmsService;
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
    private final SmsService smsService;

    @KafkaListener(topics = "notification.email", groupId = "notification-group")
    public void consumeEmailNotification(String message) {
        log.info("Received message from Kafka: {}", message);
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            emailService.sendEmail(request);
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage());
            throw new RuntimeException("Failed to process notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "notification.sms", groupId = "notification-group")
    public void consumeSmsNotification(String message) {
        log.info("Received SMS message from Kafka: {}", message);
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            smsService.sendSms(request);
        } catch (Exception e) {
            log.error("Error processing SMS notification: {}", e.getMessage());
            throw new RuntimeException("Failed to process SMS notification: " + e.getMessage());
        }
    }
}
