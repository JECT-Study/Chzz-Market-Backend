package org.chzz.market.domain.likev2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.base.entity.BaseTimeEntity;

// TODO: V2 경매 API 전환이 끝나서 운영 환경에 적용할 땐 기존 테이블에서 데이터를 이관해야 합니다.(flyway 스크립트)
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "likes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "auction_id"})}
)
public class LikeV2 extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long auctionId;
}
