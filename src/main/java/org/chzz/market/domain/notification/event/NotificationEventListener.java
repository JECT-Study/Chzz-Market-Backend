package org.chzz.market.domain.notification.event;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationRealMessage;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.notification.service.RedisPublisher;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Transactional
@Slf4j
public class NotificationEventListener {
    private final RedisPublisher redisPublisher;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener // default 인 phase = TransactionPhase.AFTER_COMMIT 사용
    public void sendNotification(final NotificationEvent notificationEvent) {
        log.info("알림 이벤트 수신 - notificationEvent = {}", notificationEvent);

        // 1. 알림 메시지 저장
        List<Notification> notifications = createNotifications(notificationEvent);
        notificationRepository.saveAll(notifications);

        // 2. 사용자 ID와 알림 ID를 매핑하는 맵 생성과 메시지 발행
        Map<Long, Long> userNotificationMap = notifications.stream()
                .collect(Collectors.toMap(Notification::getUserId, Notification::getId));

        // 3. Redis에 메시지 발행
        redisPublisher.publish(NotificationRealMessage.of(userNotificationMap, notificationEvent));
    }

    /**
     * 알림 객체 목록을 생성합니다.
     *
     * @param notificationEvent 알림 이벤트 데이터
     * @param userMap           사용자 ID와 사용자 객체의 매핑
     * @return 생성된 알림 객체 목록
     */
    private List<Notification> createNotifications(NotificationEvent notificationEvent) {
        return notificationEvent.userIds().stream()
                .map(notificationEvent::toEntity)
                .toList();
    }
}
