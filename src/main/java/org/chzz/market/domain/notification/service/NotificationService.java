package org.chzz.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.dto.NotificationRealMessage;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.dto.response.NotificationSseResponse;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.error.NotificationErrorCode;
import org.chzz.market.domain.notification.error.NotificationException;
import org.chzz.market.domain.notification.repository.EmitterRepositoryImpl;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
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
    private final RedisPublisher redisPublisher;
    private final NotificationRepository notificationRepository;
    private final EmitterRepositoryImpl emitterRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자 ID로 SSE 연결을 생성하고 구독을 처리합니다.
     *
     * @param userId 구독할 사용자 ID
     * @return SseEmitter 객체
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterRepository.save(userId, emitter);
        setupEmitterCallbacks(userId, emitter);
        sendInitialConnectionEvent(userId, emitter);
        return emitter;
    }

    @Transactional
    public void sendNotification(NotificationMessage notificationMessage) {
        // 1. 알림 메시지 저장
        Map<Long, User> userMap = userRepository.findAllById(notificationMessage.getUserIds())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<Notification> notifications = createNotifications(notificationMessage, userMap);
        notificationRepository.saveAll(notifications);

        // 2. 사용자 ID와 알림 ID를 매핑하는 맵 생성과 메시지 발행
        Map<Long, Long> userNotificationMap = notifications.stream()
                .collect(Collectors.toMap(n -> n.getUser().getId(), Notification::getId));

        // 3. Redis에 메시지 발행
        redisPublisher.publish(new NotificationRealMessage(userNotificationMap, notificationMessage.getMessage(),
                notificationMessage.getType()));
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
            } catch (IOException e) {
                log.error("Error sending SSE event to user {}", userId);
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
            emitter.complete();
            log.info("SSE connection timed out for user {}", userId);
        });
        emitter.onError((e) -> {
            emitter.complete();
            log.error("SSE connection error for user {}", userId);
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

    /**
     * 알림 객체 목록을 생성합니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     * @return 생성된 알림 객체 목록
     */
    private List<Notification> createNotifications(NotificationMessage notificationMessage,
                                                   Map<Long, User> userMap) {
        return notificationMessage.getUserIds().stream()
                .map(userId -> createNotification(notificationMessage, userMap, userId))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 알림 객체를 생성합니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     * @param userId              사용자 ID
     * @return 생성된 알림 객체, 사용자 존재 시
     */
    private Notification createNotification(NotificationMessage notificationMessage, Map<Long, User> userMap,
                                            Long userId) {
        User user = userMap.get(userId);
        return user != null ? Notification.builder()
                .message(notificationMessage.getMessage())
                .user(user)
                .product(notificationMessage.getProduct())
                .type(notificationMessage.getType())
                .build() : null;
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
