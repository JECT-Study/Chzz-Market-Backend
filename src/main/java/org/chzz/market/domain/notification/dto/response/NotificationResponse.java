package org.chzz.market.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private Boolean isRead;
    private String cdnPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long auctionId;
    private LocalDateTime createdAt;

    @QueryProjection
    public NotificationResponse(Long id, String message, String type, Boolean isRead, String cdnPath, Long auctionId,
                                LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.cdnPath = cdnPath;
        this.auctionId = auctionId;
        this.createdAt = createdAt;
    }
}
