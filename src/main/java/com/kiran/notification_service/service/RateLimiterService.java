package com.kiran.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_NOTIFICATIONS_PER_HOUR = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    public boolean isAllowed(String recipient) {
        String key = "rate_limit:notifications:" + recipient;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // First request — set expiry of 1 hour
            redisTemplate.expire(key, WINDOW);
        }

        if (count > MAX_NOTIFICATIONS_PER_HOUR) {
            log.warn("Rate limit exceeded for recipient: {}", recipient);
            return false;
        }

        log.info("Notification count for {}: {}/{}", recipient, count, MAX_NOTIFICATIONS_PER_HOUR);
        return true;
    }
}