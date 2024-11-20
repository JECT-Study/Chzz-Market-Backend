package org.chzz.market.domain.auctionv2.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.entity.Bid.BidStatus;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.likev2.entity.LikeV2;
import org.chzz.market.domain.likev2.repository.LikeV2Repository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private LikeV2Repository likeV2Repository;

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

    private AuctionV2 createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId,
                                    Integer minPrice) {
        AuctionV2 auction = AuctionV2.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .minPrice(minPrice)
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
        AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, user.getId(),
                1000);
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
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, seller.getId(),
                    1000);

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
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);

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
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
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
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);

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
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
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
                    user.getId(), 2000);
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

    @Nested
    @DisplayName("경매 목록 조회")
    class Auctions {
        @Test
        public void 정식경매_목록_조회_테스트_본인것() throws Exception {
            //given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionV2Repository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(seller.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isTrue();
        }

        @Test
        public void 정식경매_목록_조회_테스트_남의것() throws Exception {
            //given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionV2Repository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(user.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isFalse();

            //when 비로그인
            Page<OfficialAuctionResponse> result1 = auctionQueryRepository.findOfficialAuctions(null,
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isFalse();
        }

        @Test
        public void 정식경매_목록_조회_테스트_입찰을했을때() throws Exception {
            //given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionV2Repository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));
            Bid bid = createBid(user, auction, 1000L, BidStatus.ACTIVE);
            bidRepository.save(bid);
            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(user.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isTrue();
        }

        @Test
        public void 사전경매_목록조회_좋아요를_했을때() {
            //given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            auctionV2Repository.save(auction);
            LikeV2 like = LikeV2.builder().auctionId(auction.getId()).userId(user.getId()).build();
            likeV2Repository.save(like);

            //when
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));
            Page<PreAuctionResponse> result = auctionQueryRepository.findPreAuctions(user.getId(),
                    Category.ELECTRONICS, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsLiked()).isTrue();
        }

        @Test
        public void 사전경매_목록조회_좋아요를_안했을때() {
            //given
            AuctionV2 auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            auctionV2Repository.save(auction);

            //when
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));
            Page<PreAuctionResponse> resultWithUserId = auctionQueryRepository.findPreAuctions(user.getId(),
                    Category.ELECTRONICS, pageable);

            assertThat(resultWithUserId).isNotNull();
            assertThat(resultWithUserId.getContent()).hasSize(1);
            assertThat(resultWithUserId.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(resultWithUserId.getContent().get(0).getIsSeller()).isFalse();
            assertThat(resultWithUserId.getContent().get(0).getIsLiked()).isFalse();

            // when - 비로그인
            Page<PreAuctionResponse> resultWithNull = auctionQueryRepository.findPreAuctions(null, Category.ELECTRONICS,
                    pageable);

            assertThat(resultWithNull).isNotNull();
            assertThat(resultWithNull.getContent()).hasSize(1);
            assertThat(resultWithNull.getContent().get(0).getProductName()).isEqualTo("맥북프로");
            assertThat(resultWithNull.getContent().get(0).getIsSeller()).isFalse();
            assertThat(resultWithNull.getContent().get(0).getIsLiked()).isFalse();
        }

        @Test
        public void 정식경매_목록_조회_정렬_테스트() throws Exception {
            //given
            AuctionV2 auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    1000);
            AuctionV2 auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    2000);
            auctionV2Repository.save(auction1);
            auctionV2Repository.save(auction2);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive-v2"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(seller.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, pageable);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("아이패드"); // 가격이 더 높은 아이패드가 먼저
            assertThat(result.getContent().get(1).getProductName()).isEqualTo("맥북프로"); // 가격이 낮은 맥북프로가 나중
        }
    }
}
