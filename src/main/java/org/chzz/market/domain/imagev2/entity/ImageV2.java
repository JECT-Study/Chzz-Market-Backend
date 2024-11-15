package org.chzz.market.domain.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.base.entity.BaseTimeEntity;

// TODO: V2 경매 API 전환이 끝나서 운영 환경에 적용할 땐 기존 테이블에서 데이터를 이관해야 합니다.(flyway 스크립트)
@Getter
@Entity
@Table(indexes = {
        @Index(columnList = "auction_id, image_id, cdn_path")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ImageV2 extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private AuctionV2 auction;

    @Column(nullable = false)
    private String cdnPath;

    @Column
    private int sequence;

    public void specifyAuction(AuctionV2 auction) {
        this.auction = auction;
    }

    public void changeSequence(Integer newSequence) {
        this.sequence = newSequence;
    }
}
