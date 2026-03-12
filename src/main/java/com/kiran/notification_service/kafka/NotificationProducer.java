package com.kiran.notification_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendNotification(String topic, String message) {
        log.info("Sending message to topic: {}, message: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
