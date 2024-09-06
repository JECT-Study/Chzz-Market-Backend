package org.chzz.market.domain.notification.repository;

import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.notification.entity.QNotification.notification;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.dto.response.QNotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<NotificationResponse> findByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(notification)
                .where(notification.user.id.eq(userId));

        List<NotificationResponse> content = baseQuery
                .select(new QNotificationResponse(
                        notification.id,
                        notification.message,
                        notification.type,
                        notification.isRead,
                        image.cdnPath,
                        notification.createdAt
                ))
                .leftJoin(notification.image, image)
                .where(notification.isDeleted.eq(false))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notification.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(notification.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

    }
}
