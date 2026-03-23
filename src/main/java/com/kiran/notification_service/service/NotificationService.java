package com.kiran.notification_service.service;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.entity.NotificationLog;
import com.kiran.notification_service.enums.NotificationStatus;
import com.kiran.notification_service.kafka.NotificationProducer;
import com.kiran.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;
    private final RateLimiterService rateLimiterService;


    public void sendNotification(NotificationRequest request) {

        // 1. Check rate limit
        if (!rateLimiterService.isAllowed(request.getRecipient())) {
            throw new RuntimeException("Rate limit exceeded for recipient: " + request.getRecipient());
        }

        // 1. Save to DB with PENDING status
        NotificationLog logdb = NotificationLog.builder()
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        notificationLogRepository.save(logdb);

        // 2. Send to Kafka topic based on channel
        try {
            String topic = "notification." + request.getChannel().name().toLowerCase();
            String message = objectMapper.writeValueAsString(request);
            notificationProducer.sendNotification(topic, message);
            log.info("Notification sent to Kafka topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send notification to Kafka: {}", e.getMessage());
        }
    }
}
