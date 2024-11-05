package org.chzz.market.domain.notification.repository;

import static java.lang.Boolean.FALSE;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.notification.entity.QNotification.notification;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.notification.dto.response.NotificationResponse;
import org.chzz.market.domain.notification.dto.response.QNotificationResponse;
import org.chzz.market.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<NotificationResponse> findByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(notification)
                .where(notification.userId.eq(userId));

        List<NotificationResponse> content = baseQuery
                .select(new QNotificationResponse(
                        notification.id,
                        notification.message,
                        notification.type,
                        notification.isRead,
                        notification.cdnPath,
                        getAuctionIdPath(),
                        notification.createdAt
                ))
                .where(notification.isDeleted.eq(FALSE))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notification.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(notification.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

    }

    private PathBuilder<Long> getAuctionIdPath() {
        return new PathBuilder<>(Notification.class, "notification").get("auctionId",
                Long.class); // auctionId는 Notification 부모클래스에는 없는 필드이므로 PathBuilder를 사용하여 직접 생성
    }
}
