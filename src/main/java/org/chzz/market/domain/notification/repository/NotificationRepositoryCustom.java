package org.chzz.market.domain.notification.repository;

import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {
    Page<NotificationResponse> findByUserId(Long userId, Pageable pageable);
}
