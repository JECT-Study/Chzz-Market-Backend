package org.chzz.market.domain.notification.repository;

import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.notification.entity.QNotification.notification;
import static org.chzz.market.domain.product.entity.QProduct.product;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.entity.QImage;
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
                .leftJoin(notification.product, product)
                .leftJoin(image).on(image.id.eq(getFirstImageId()))
                .where(notification.isDeleted.eq(false))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notification.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(notification.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

    }

    /**
     * 상품의 첫 번째 이미지를 조회합니다.
     *
     * @return 첫 번째 이미지 ID
     */
    private JPQLQuery<Long> getFirstImageId() {
        QImage imageSub = new QImage("imageSub");
        return JPAExpressions.select(imageSub.id.min())
                .from(imageSub)
                .where(imageSub.product.id.eq(product.id));
    }
}
