package com.kiran.notification_service.repository;

import com.kiran.notification_service.entity.NotificationLog;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    NotificationLog findTopByRecipientAndSubjectOrderByCreatedAtDesc(@NotBlank(message = "Recipient is required") String recipient, @NotBlank(message = "Subject is required") String subject);
}
