package org.chzz.market.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.chzz.market.domain.notification.entity.AuctionSuccessNotification;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.error.NotificationErrorCode;
import org.chzz.market.domain.notification.error.NotificationException;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification, readNotification, deletedNotification;

    @BeforeEach
    void setUp() {
        user = User.builder().providerId("1234").nickname("닉네임1").email("asd@naver.com").id(1L).build();
        notification = new AuctionSuccessNotification(user.getId(), null, "Test Notification 1", 1L);
        readNotification = new AuctionSuccessNotification(user.getId(), null, "Test Notification 1", 1L);
        deletedNotification = new AuctionSuccessNotification(user.getId(), null, "Test Notification 1", 1L);
        deletedNotification.delete();
        readNotification.read();
        notificationRepository.save(notification);
        notificationRepository.save(readNotification);
        notificationRepository.save(deletedNotification);
    }

    @Test
    @DisplayName("정상적으로 알림을 읽을 수 있다.")
    public void shouldReadNotificationSuccessfully() {
        // given
        given(notificationRepository.findById(notification.getId())).willReturn(Optional.of(notification));

        // when
        notificationService.readNotification(user.getId(), notification.getId());

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("삭제된 알림을 읽으려고 하면 예외가 발생한다.")
    public void shouldThrowExceptionForDeletedNotificationRead() {
        // given
        given(notificationRepository.findById(deletedNotification.getId())).willReturn(
                Optional.of(deletedNotification));

        // when & then
        assertThatThrownBy(() -> notificationService.readNotification(user.getId(), deletedNotification.getId()))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(NotificationErrorCode.DELETED_NOTIFICATION);
    }

    @Test
    @DisplayName("권한이 없는 사용자가 알림을 읽으려고 하면 예외가 발생한다.")
    public void shouldThrowExceptionForUnauthorizedUserReadingNotification() {
        // given
        given(notificationRepository.findById(notification.getId())).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.readNotification(3L, notification.getId()))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(NotificationErrorCode.UNAUTHORIZED_ACCESS);
    }

    @Test
    @DisplayName("정상적으로 알림을 삭제할 수 있다.")
    public void shouldDeleteNotificationSuccessfully() {
        // given
        given(notificationRepository.findById(notification.getId())).willReturn(Optional.of(notification));

        // when
        notificationService.deleteNotification(user.getId(), notification.getId());

        // then
        assertThat(notification.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("이미 삭제된 알림을 삭제하려고 하면 예외가 발생한다.")
    public void shouldThrowExceptionForAlreadyDeletedNotification() {
        // given
        given(notificationRepository.findById(deletedNotification.getId())).willReturn(
                Optional.of(deletedNotification));

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(user.getId(), deletedNotification.getId()))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(NotificationErrorCode.DELETED_NOTIFICATION);
    }


    @Test
    @DisplayName("권한이 없는 사용자가 알림을 삭제하려고 하면 예외가 발생한다.")
    public void shouldThrowExceptionForUnauthorizedUserDeletingNotification() {
        // given
        given(notificationRepository.findById(notification.getId())).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(3L, notification.getId()))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(NotificationErrorCode.UNAUTHORIZED_ACCESS);
    }

}
