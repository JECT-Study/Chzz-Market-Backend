package org.chzz.market.domain.auctionv2.entity;

import static org.chzz.market.domain.auctionv2.entity.AuctionStatus.ENDED;
import static org.chzz.market.domain.auctionv2.entity.AuctionStatus.PRE;
import static org.chzz.market.domain.auctionv2.entity.AuctionStatus.PROCEEDING;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ALREADY_OFFICIAL;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ENDED;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_ENDED;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.imagev2.error.ImageErrorCode;
import org.chzz.market.domain.imagev2.error.exception.ImageException;
import org.chzz.market.domain.user.entity.User;
import org.hibernate.annotations.DynamicUpdate;

// TODO: V2 경매 API 전환이 끝나서 운영 환경에 적용할 땐 기존 테이블에서 데이터를 이관해야 합니다.(flyway 스크립트)
@Table(name = "auction_v2")
@Entity
//@EntityListeners(value = AuctionEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicUpdate
@Getter
@Slf4j
public class AuctionV2 extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column
    private Integer minPrice;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private LocalDateTime endDateTime;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Column
    private Long winnerId;

    @Builder.Default
    @Column
    private Long likeCount = 0L;

    @Builder.Default
    @Column
    private Long bidCount = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "auction", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<ImageV2> images = new ArrayList<>();

    public void addImage(ImageV2 image) {
        images.add(image);
        image.specifyAuction(this);
    }

    public boolean isOwner(Long userId) {
        return seller.getId().equals(userId);
    }

    public void validateOwner(Long userId) {
        if (!isOwner(userId)) {
            throw new AuctionException(AUCTION_ACCESS_FORBIDDEN);
        }
    }

    public boolean isPreAuction() {
        return status == PRE;
    }

    public boolean isOfficialAuction() {
        return status == PROCEEDING || status == ENDED;
    }

    public void validateAuctionEnded() {
        if (!status.equals(ENDED)) {
            throw new AuctionException(AUCTION_NOT_ENDED);
        }
    }

    public boolean isWinner(Long userId) {
        return winnerId != null && winnerId.equals(userId);
    }

    public void startOfficialAuction() {
        if (isOfficialAuction()) {
            throw new AuctionException(AUCTION_ALREADY_OFFICIAL);
        }
        this.status = PROCEEDING;
        this.endDateTime = LocalDateTime.now().plusDays(1);
    }

    public String getFirstImageCdnPath() {
        return images.stream()
                .filter(image -> image.getSequence() == 1)
                .map(ImageV2::getCdnPath)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("경매의 첫 번째 이미지가 없는 경우: {}", this.id);
                    return new ImageException(ImageErrorCode.IMAGE_NOT_FOUND);
                });
    }

    public void validateAuctionEndTime() {
        // 경매가 진행중이 아닐 때
        if (status != PROCEEDING || endDateTime == null || LocalDateTime.now().isAfter(endDateTime)) {
            throw new AuctionException(AUCTION_ENDED);
        }
    }

    public boolean isAboveMinPrice(Long amount) {
        return amount >= minPrice;
    }

    public void addImages(final List<ImageV2> images) {
        this.images.addAll(images);
    }

    public void endAuction() {
        this.status = ENDED;
    }

    public void assignWinner(final Long bidderId) {
        this.winnerId = bidderId;
    }
}
