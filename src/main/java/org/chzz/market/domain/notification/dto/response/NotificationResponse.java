package org.chzz.market.domain.notification.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.notification.entity.NotificationType;

@Getter
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private String cdnPath;
    private LocalDateTime createdAt;

    @QueryProjection
    public NotificationResponse(Long id, String message, NotificationType type, Boolean isRead, String cdnPath,
                                LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.cdnPath = cdnPath;
        this.createdAt = createdAt;
    }
}
