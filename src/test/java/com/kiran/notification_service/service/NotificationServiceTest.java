package com.kiran.notification_service.service;

import tools.jackson.databind.ObjectMapper;import com.kiran.notification_service.dto.NotificationRequest;
import com.kiran.notification_service.entity.NotificationLog;
import com.kiran.notification_service.enums.NotificationChannel;
import com.kiran.notification_service.enums.NotificationStatus;
import com.kiran.notification_service.kafka.NotificationProducer;
import com.kiran.notification_service.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationProducer notificationProducer;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Mock
    private RateLimiterService rateLimiterService;


    private NotificationService notificationService;

    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        notificationService = new NotificationService(
                notificationLogRepository,
                notificationProducer,
                objectMapper,
                rateLimiterService
        );

        request = new NotificationRequest();
        request.setRecipient("kiran@gmail.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");
        request.setChannel(NotificationChannel.EMAIL);
    }

    @Test
    void sendNotification_Success() throws Exception {
        // Arrange
        when(rateLimiterService.isAllowed(request.getRecipient())).thenReturn(true);
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenReturn(new NotificationLog());

        // Act
        notificationService.sendNotification(request);

        // Assert
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
        verify(notificationProducer, times(1)).sendNotification(anyString(), anyString());
    }

    @Test
    void sendNotification_RateLimitExceeded() {
        // Arrange
        when(rateLimiterService.isAllowed(request.getRecipient())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendNotification(request));

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        verify(notificationLogRepository, never()).save(any());
        verify(notificationProducer, never()).sendNotification(anyString(), anyString());
    }
}