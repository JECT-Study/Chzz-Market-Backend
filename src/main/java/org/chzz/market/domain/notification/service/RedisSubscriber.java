package org.chzz.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RedisSubscriber {

    private final NotificationService notificationService;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper;

    /**
     * Redis에서 발행된 메시지를 수신하고 사용자에게 알림을 보냅니다.
     *
     * @param message 수신한 메시지
     */
    public void onMessage(String message) {
        executor.execute(() -> {
            try {
                NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
                notificationMessage.getUserIds()
                        .forEach(
                                userId -> notificationService.sendRealTimeNotification(notificationMessage.getMessage(),
                                        userId));
            } catch (Exception e) {
                log.error("Error handling message");
            }
        });
    }

}
