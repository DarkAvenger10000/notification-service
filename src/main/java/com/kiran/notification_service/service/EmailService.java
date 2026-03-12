package com.kiran.notification_service.service;

import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.entity.NotificationLog;
import com.kiran.notification_service.enums.NotificationStatus;
import com.kiran.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    public void sendEmail(NotificationRequest request) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(request.getRecipient());
            mailMessage.setSubject(request.getSubject());
            mailMessage.setText(request.getMessage());

            mailSender.send(mailMessage);
            log.info("Email sent successfully to: {}", request.getRecipient());

            // Update status to SENT
            updateNotificationStatus(request, NotificationStatus.SENT, null);

        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", request.getRecipient(), e.getMessage());
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
