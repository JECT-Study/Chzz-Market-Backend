package org.chzz.market.domain.auctionv2.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.orderv2.entity.OrderV2;
import org.chzz.market.domain.orderv2.repository.OrderV2Repository;
import org.chzz.market.domain.paymentv2.entity.PaymentV2.PaymentMethod;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuctionV2QueryRepositoryTest {
    @Autowired
    private AuctionV2QueryRepository auctionQueryRepository;
    @Autowired
    private AuctionV2Repository auctionV2Repository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderV2Repository orderRepository;

    private User seller;
    private User user;
    private ImageV2 defaultImage;

    @BeforeEach
    void setUp() {
        seller = User.builder().email("seller").providerId("seller").providerType(User.ProviderType.KAKAO).build();
        user = User.builder().email("user").providerId("user").providerType(User.ProviderType.KAKAO).build();
        defaultImage = ImageV2.builder().cdnPath("https://cdn.com").sequence(1).build();

        userRepository.save(seller);
        userRepository.save(user);
    }

    private AuctionV2 createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId) {
        AuctionV2 auction = AuctionV2.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .build();
        auction.addImage(defaultImage);
        auctionV2Repository.save(auction);
        return auction;
    }

    private Bid createBid(User bidder, AuctionV2 auction, Long amount, Bid.BidStatus status) {
        Bid bid = Bid.builder()
                .bidderId(bidder.getId())
                .auctionId(auction.getId())
                .amount(amount)
                .status(status)
                .build();
        bidRepository.save(bid);
        return bid;
    }

    private OrderV2 createOrder(AuctionV2 auction, User buyer, Long amount) {
        OrderV2 order = OrderV2.builder()
                .auction(auction)
                .buyerId(buyer.getId())
                .amount(amount)
                .paymentId(1L)
                .roadAddress("서울시 강남구")
                .jibun("123")
                .zipcode("123")
                .detailAddress("123")
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .method(PaymentMethod.CARD)
                .orderNo("123")
                .build();
        orderRepository.save(order);
        return order;
    }

    @Test
    void 낙찰정보를_조회한다() {
        // Given
        AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, user.getId());
        createBid(user, auction, 2000L, Bid.BidStatus.ACTIVE);

        // When
        Optional<WonAuctionDetailsResponse> result = auctionQueryRepository.findWinningBidById(auction.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().winningAmount()).isEqualTo(2000L);
    }

    @Nested
    @DisplayName("정식 경매 상세정보조회")
    class OfficialAuctionDetail {
        @Test
        void 본인의_제품을_조회한경우() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, seller.getId());

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    seller.getId(), auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsSeller()).isTrue();
            assertThat(response.getIsParticipated()).isFalse();
            assertThat(response.getIsWon()).isTrue();
            assertThat(response.getIsOrdered()).isFalse();
        }

        @Test
        void 다른_사람_경매를_참여안한경우_조회한경우() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null);

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    user.getId(), auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsSeller()).isFalse();
            assertThat(response.getIsParticipated()).isFalse();
        }

        @Test
        void 다른_사람_경매을_참여한경우_조회() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null);
            createBid(user, auction, 1000L, Bid.BidStatus.ACTIVE);

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    user.getId(), auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsSeller()).isFalse();
            assertThat(response.getIsParticipated()).isTrue();
        }

        @Test
        void 없는_경매_인경우() {
            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    user.getId(), -1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void 비로그인_상태에서_조회한_경우() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null);

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    null, auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsSeller()).isFalse();
            assertThat(response.getIsParticipated()).isFalse();
            assertThat(response.getBidId()).isNull();
            assertThat(response.getBidAmount()).isEqualTo(0L);
            assertThat(response.getRemainingBidCount()).isEqualTo(3);
        }

        @Test
        void 취소된_입찰이_있는_경우() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null);
            createBid(user, auction, 2000L, Bid.BidStatus.CANCELLED);

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    user.getId(), auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsCancelled()).isTrue();
        }

        @Test
        void 주문이_있을시_조회를_한다() {
            // Given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING,
                    user.getId());
            createBid(user, auction, 2000L, Bid.BidStatus.ACTIVE);
            createOrder(auction, user, 2000L);

            // When
            Optional<OfficialAuctionDetailResponse> result = auctionQueryRepository.findOfficialAuctionDetailById(
                    user.getId(), auction.getId());

            // Then
            OfficialAuctionDetailResponse response = result.get();
            assertThat(response).isNotNull();
            assertThat(response.getIsSeller()).isFalse();
            assertThat(response.getIsParticipated()).isTrue();
            assertThat(response.getIsWon()).isTrue();
            assertThat(response.getIsWinner()).isTrue();
            assertThat(response.getIsOrdered()).isTrue();
        }
    }
}
