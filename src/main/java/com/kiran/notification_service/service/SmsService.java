package com.kiran.notification_service.service;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.entity.NotificationLog;
import com.kiran.notification_service.enums.NotificationStatus;
import com.kiran.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private final NotificationLogRepository notificationLogRepository;

    public void sendSms(NotificationRequest request) {
        try {
            // Mock SMS sending — replace with Twilio/AWS SNS in production
            log.info("Sending SMS to: {}", request.getRecipient());
            log.info("SMS Content: {}", request.getMessage());

            // Simulate processing time
            Thread.sleep(100);

            log.info("SMS sent successfully to: {}", request.getRecipient());
            updateNotificationStatus(request, NotificationStatus.SENT, null);

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}, error: {}", request.getRecipient(), e.getMessage());
            updateNotificationStatus(request, NotificationStatus.FAILED, e.getMessage());
        }
    }

    private void updateNotificationStatus(NotificationRequest request,
                                          NotificationStatus status,
                                          String errorMessage) {
        NotificationLog notificationLog = notificationLogRepository
                .findTopByRecipientAndSubjectOrderByCreatedAtDesc(
                        request.getRecipient(),
                        request.getSubject()
                );

        if (notificationLog != null) {
            notificationLog.setStatus(status);
            notificationLog.setErrorMessage(errorMessage);
            notificationLog.setUpdatedAt(LocalDateTime.now());
            notificationLogRepository.save(notificationLog);
        }
    }
}