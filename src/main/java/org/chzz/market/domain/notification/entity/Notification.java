package org.chzz.market.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.error.NotificationErrorCode;
import org.chzz.market.domain.notification.error.NotificationException;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public void read() {
        if (this.isDeleted) {
            throw new NotificationException(NotificationErrorCode.DELETED_NOTIFICATION);
        }
        if (this.isRead) {
            return;
        }
        this.isRead = true;
    }

    public void delete() {
        if (this.isDeleted) {
            throw new NotificationException(NotificationErrorCode.DELETED_NOTIFICATION);
        }
        this.isDeleted = true;
    }

}
