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
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.base.entity.BaseTimeEntity;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_auction_image", columnList = "image_id, cdn_path, auction_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Image extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Column(nullable = false)
    private String cdnPath;

    @Column
    private int sequence;

    public void specifyAuction(Auction auction) {
        this.auction = auction;
    }

    public void changeSequence(Integer newSequence) {
        this.sequence = newSequence;
    }
}
