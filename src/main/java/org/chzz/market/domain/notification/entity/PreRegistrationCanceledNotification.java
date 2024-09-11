package org.chzz.market.domain.notification.entity;

import static org.chzz.market.domain.notification.entity.NotificationType.Values.PRE_REGISTRATION_CANCELED;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = PRE_REGISTRATION_CANCELED)
public class PreRegistrationCanceledNotification extends Notification {

    public PreRegistrationCanceledNotification(User user, Image image, String message) {
        super(user, image, message);
    }
}
