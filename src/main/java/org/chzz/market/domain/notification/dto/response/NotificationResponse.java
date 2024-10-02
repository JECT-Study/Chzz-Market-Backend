package org.chzz.market.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private String type;
    private Boolean isRead;
    private String imageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long auctionId;
    private LocalDateTime createdAt;

    @QueryProjection
    public NotificationResponse(Long notificationId, String message, String type, Boolean isRead, String imageUrl, Long auctionId,
                                LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.imageUrl = imageUrl;
        this.auctionId = auctionId;
        this.createdAt = createdAt;
    }
}
