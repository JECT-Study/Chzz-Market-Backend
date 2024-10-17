package org.chzz.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.dto.response.NotificationSseResponse;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.error.NotificationErrorCode;
import org.chzz.market.domain.notification.error.NotificationException;
import org.chzz.market.domain.notification.repository.EmitterRepositoryImpl;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.token.entity.TokenType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmitterRepositoryImpl emitterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자 ID로 SSE 연결을 생성하고 구독을 처리합니다.
     *
     * @param userId 구독할 사용자 ID
     * @return SseEmitter 객체
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TokenType.ACCESS.getExpirationTime() * 1000L);
        emitterRepository.save(userId, emitter);
        setupEmitterCallbacks(userId, emitter);
        sendInitialConnectionEvent(userId, emitter);
        return emitter;
    }

    /**
     * 실시간으로 SSE를 통해 사용자에게 알림을 전송합니다.
     *
     * @param sseResponse 전송할 알림 메시지 객체
     * @param userId      사용자 ID
     */
    public void sendRealTimeNotification(Long userId, NotificationSseResponse sseResponse) {
        Optional<SseEmitter> findEmitter = emitterRepository.findById(userId);
        findEmitter.ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(userId + "_" + Instant.now().toEpochMilli())
                        .name("notification")
                        .data(objectMapper.writeValueAsString(sseResponse)));
                log.info("SSE 전송: userId: {}, sseResponse: {}", userId, sseResponse);
            } catch (IOException e) {
                // 내부에서 추가로 IOException이 발생하므로, 프레임워크의 예외 처리 핸들러에 처리
                log.info("사용자 ID {}의 끊어진 SSE 연결을 정리합니다.", userId);
            }
        });
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        Notification notification = findNotificationByUserAndId(userId, notificationId);
        notification.read();
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = findNotificationByUserAndId(userId, notificationId);
        notification.delete();
    }

    /**
     * SseEmitter의 콜백을 설정합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 설정할 SseEmitter
     */
    private void setupEmitterCallbacks(Long userId, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            emitterRepository.deleteById(userId);
            log.info("SSE connection completed for user {}", userId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out for user {}", userId);
        });
    }

    /**
     * 초기 연결 시 더미 이벤트를 전송하여 503 에러를 방지합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 이벤트를 전송할 SseEmitter
     */
    private void sendInitialConnectionEvent(Long userId, SseEmitter emitter) {
        try {
            log.info("User {} subscribed to notifications with initial connection", userId);
            emitter.send(SseEmitter.event()
                    .id(userId + "_" + Instant.now().toEpochMilli())
                    .name("init")
                    .data("Connection Established"));
        } catch (Exception e) {
            log.error("Error sending initial connection event to user {}", userId);
        }
    }

    private Notification findNotificationByUserAndId(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getUser().getId() != userId) {
            throw new NotificationException(NotificationErrorCode.UNAUTHORIZED_ACCESS);
        }
        return notification;
    }
}
