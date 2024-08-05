package org.chzz.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.repository.EmitterRepositoryImpl;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RedisSubscriber {

    private final EmitterRepositoryImpl emitterRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Redis에서 발행된 메시지를 수신하고 사용자에게 알림을 보냅니다.
     *
     * @param message 수신한 메시지
     */
    public void onMessage(String message) {
        executor.execute(() -> {
            try {
                NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
                List<User> users = userRepository.findAllById(notificationMessage.getUserIds());
                Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
                sendUsersNotification(notificationMessage, userMap);
            } catch (Exception e) {
                log.error("Error handling message");
            }
        });
    }

    /**
     * 사용자에게 알림을 보냅니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     */
    private void sendUsersNotification(NotificationMessage notificationMessage, Map<Long, User> userMap) {
        List<Notification> notifications = createNotifications(notificationMessage, userMap);
        notificationRepository.saveAll(notifications);
        notificationMessage.getUserIds()
                .forEach(userId -> sendRealTimeNotification(notificationMessage.getMessage(), userId));
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
                .type(notificationMessage.getType())
                .build() : null;
    }

    /**
     * 실시간으로 SSE를 통해 사용자에게 알림을 전송합니다.
     *
     * @param message 전송할 알림 메시지
     * @param userId  사용자 ID
     */
    private void sendRealTimeNotification(String message, Long userId) {
        Optional<SseEmitter> findEmitter = emitterRepository.findById(userId);
        findEmitter.ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(userId + "_" + System.currentTimeMillis())
                        .name("notification")
                        .data(message));
            } catch (Exception e) {
                log.error("Error sending sendRealTimeNotification");
            }
        });
    }

}
