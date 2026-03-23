package com.kiran.notification_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Test
    void isAllowed_FirstRequest_ShouldAllow() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Act
        boolean result = rateLimiterService.isAllowed("kiran@gmail.com");

        // Assert
        assertTrue(result);
        verify(redisTemplate, times(1)).expire(anyString(), any());
    }

    @Test
    void isAllowed_WithinLimit_ShouldAllow() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(3L);

        // Act
        boolean result = rateLimiterService.isAllowed("kiran@gmail.com");

        // Assert
        assertTrue(result);
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    void isAllowed_ExceedsLimit_ShouldBlock() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(6L);

        // Act
        boolean result = rateLimiterService.isAllowed("kiran@gmail.com");

        // Assert
        assertFalse(result);
    }
}