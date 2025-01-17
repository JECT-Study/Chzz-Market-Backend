package org.chzz.market.domain.notification.controller;

import static org.chzz.market.common.error.GlobalErrorCode.AUTHENTICATION_REQUIRED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @Override
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@LoginUser Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    @Override
    @GetMapping(value = "/subscribe", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@LoginUser Long userId, HttpServletResponse response) {
        if (userId == null) {
            throw new GlobalException(AUTHENTICATION_REQUIRED);
        }
        response.setHeader("X-Accel-Buffering", "no");
        return notificationService.subscribe(userId);
    }

    @Override
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@LoginUser Long userId, @PathVariable Long notificationId) {
        notificationService.readNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@LoginUser Long userId, @PathVariable Long notificationId) {
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }
}
