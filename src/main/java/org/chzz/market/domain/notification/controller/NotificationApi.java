package org.chzz.market.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "notifications", description = "알림 API")
public interface NotificationApi {
    @Operation(summary = "알림 목록 조회")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "SSE 알림 구독")
    public SseEmitter subscribe(Long userId, HttpServletResponse response);

    @Operation(summary = "알림 읽음")
    public ResponseEntity<Void> readNotification(Long userId, Long notificationId);

    @Operation(summary = "알림 삭제")
    public ResponseEntity<Void> deleteNotification(Long userId, Long notificationId);
}
