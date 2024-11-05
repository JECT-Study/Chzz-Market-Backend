package org.chzz.market.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.entity.AuctionSuccessNotification;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@DatabaseTest
@Transactional
class NotificationRepositoryImplTest {

    @Autowired
    NotificationRepository notificationRepository;

    private User user1;

    @BeforeEach
    void setUp(@Autowired NotificationRepository notificationRepository,
               @Autowired UserRepository userRepository) {
        user1 = User.builder().providerId("1234").nickname("닉네임1").email("asd@naver.com").build();
        userRepository.save(user1);

        // 몇 개의 알림을 미리 저장
        Notification notification1 = new AuctionSuccessNotification(user1.getId(), null, "Test Notification 1", 1L);

        Notification notification2 = new AuctionSuccessNotification(user1.getId(), null, "Test Notification 1", 1L);

        Notification notification3 = new AuctionSuccessNotification(user1.getId(), null, "Test Notification 1", 1L);

        notification3.delete();
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);
    }

    @Test
    @DisplayName("특정 사용자의 알림을 조회할 수 있다.(삭제된 알림 필터링)")
    public void shouldRetrieveNotificationsForSpecificUser() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<NotificationResponse> notifications = notificationRepository.findByUserId(user1.getId(), pageable);

        // then
        assertThat(notifications).isNotNull();
        assertThat(notifications.getTotalElements()).isEqualTo(2);
        assertThat(notifications.getContent()).hasSize(2);
    }

}
