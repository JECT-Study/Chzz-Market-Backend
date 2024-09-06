package org.chzz.market.domain.notification.service;

import static org.chzz.market.domain.notification.error.NotificationErrorCode.REDIS_MESSAGE_SEND_FAILURE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationRealMessage;
import org.chzz.market.domain.notification.error.NotificationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper;

    public void publish(NotificationRealMessage notificationRealMessage) {
        try {
            String message = objectMapper.writeValueAsString(notificationRealMessage);
            sendMessageToRedis(message, notificationRealMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NotificationRealMessage to JSON. NotificationRealMessage: {}. Error: {}",
                    notificationRealMessage, e.getMessage(), e);
            throw new NotificationException(REDIS_MESSAGE_SEND_FAILURE); // 객체 자체의 문제임으로 예외를 던져 롤백 처리
        }
    }

    @Retryable(
            retryFor = {RedisException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    private void sendMessageToRedis(String message, NotificationRealMessage notificationRealMessage) {
        try {
            redisTemplate.convertAndSend(topic.getTopic(), message);
        } catch (RedisException e) {
            log.error("Failed to send message to Redis. NotificationRealMessage: {}. Error: {}",
                    notificationRealMessage, e.getMessage(), e); //예외를 던지지 않음으로 Redis 메세지 발행에 실패해도 알림 저장을 가능하게 함
        }
    }
}
