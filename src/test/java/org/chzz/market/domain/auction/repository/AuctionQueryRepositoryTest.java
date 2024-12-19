package org.chzz.market.domain.auction.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.common.CustomSpringBootTest;
import org.chzz.market.domain.auction.dto.response.EndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auction.dto.response.ProceedingAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.entity.Bid.BidStatus;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.order.entity.Order;
import org.chzz.market.domain.order.repository.OrderRepository;
import org.chzz.market.domain.payment.entity.Payment.PaymentMethod;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@CustomSpringBootTest
@Transactional
class AuctionQueryRepositoryTest {
    @Autowired
    private AuctionQueryRepository auctionQueryRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private LikeRepository likeRepository;

    private User seller;
    private User user, user1;
    private Image defaultImage;

    @BeforeEach
    void setUp() {
        seller = User.builder().email("seller").providerId("seller").providerType(User.ProviderType.KAKAO).build();
        user = User.builder().email("user").providerId("user").providerType(User.ProviderType.KAKAO).build();
        user1 = User.builder().email("user1").providerId("user1").providerType(User.ProviderType.KAKAO).build();
        defaultImage = Image.builder().cdnPath("https://cdn.com").sequence(1).build();

        userRepository.saveAll(List.of(seller, user, user1));
    }

    private Auction createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId,
                                  Integer minPrice) {
        Auction auction = Auction.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .minPrice(minPrice)
                .build();
        auction.addImage(defaultImage);
        auctionRepository.save(auction);
        return auction;
    }

    private Bid createBid(User bidder, Auction auction, Long amount, Bid.BidStatus status) {
        Bid bid = Bid.builder()
                .bidderId(bidder.getId())
                .auctionId(auction.getId())
                .amount(amount)
                .status(status)
                .build();
        bidRepository.save(bid);
        return bid;
    }

    private Order createOrder(Auction auction, User buyer, Long amount) {
        Order order = Order.builder()
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
        Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, user.getId(),
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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, seller.getId(),
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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);

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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);

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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING,
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
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionRepository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(seller.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, null, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isTrue();
        }

        @Test
        public void 정식경매_목록_조회_테스트_남의것() throws Exception {
            //given
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionRepository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(user.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, null, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isFalse();

            //when 비로그인
            Page<OfficialAuctionResponse> result1 = auctionQueryRepository.findOfficialAuctions(null,
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, null, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isFalse();
        }

        @Test
        public void 정식경매_목록_조회_테스트_입찰을했을때() throws Exception {
            //given
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null, 1000);
            auctionRepository.save(auction);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));
            Bid bid = createBid(user, auction, 1000L, BidStatus.ACTIVE);
            bidRepository.save(bid);
            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(user.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, null, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsParticipated()).isTrue();
        }

        @Test
        public void 사전경매_목록조회_좋아요를_했을때() {
            //given
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            auctionRepository.save(auction);
            Like like = Like.builder().auctionId(auction.getId()).userId(user.getId()).build();
            likeRepository.save(like);

            //when
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));
            Page<PreAuctionResponse> result = auctionQueryRepository.findPreAuctions(user.getId(),
                    Category.ELECTRONICS, pageable);
            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(result.getContent().get(0).getIsSeller()).isFalse();
            assertThat(result.getContent().get(0).getIsLiked()).isTrue();
        }

        @Test
        public void 사전경매_목록조회_좋아요를_안했을때() {
            //given
            Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            auctionRepository.save(auction);

            //when
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));
            Page<PreAuctionResponse> resultWithUserId = auctionQueryRepository.findPreAuctions(user.getId(),
                    Category.ELECTRONICS, pageable);

            assertThat(resultWithUserId).isNotNull();
            assertThat(resultWithUserId.getContent()).hasSize(1);
            assertThat(resultWithUserId.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(resultWithUserId.getContent().get(0).getIsSeller()).isFalse();
            assertThat(resultWithUserId.getContent().get(0).getIsLiked()).isFalse();

            // when - 비로그인
            Page<PreAuctionResponse> resultWithNull = auctionQueryRepository.findPreAuctions(null, Category.ELECTRONICS,
                    pageable);

            assertThat(resultWithNull).isNotNull();
            assertThat(resultWithNull.getContent()).hasSize(1);
            assertThat(resultWithNull.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");
            assertThat(resultWithNull.getContent().get(0).getIsSeller()).isFalse();
            assertThat(resultWithNull.getContent().get(0).getIsLiked()).isFalse();
        }

        @Test
        public void 정식경매_목록_조회_정렬_테스트() throws Exception {
            //given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    2000);
            auctionRepository.save(auction1);
            auctionRepository.save(auction2);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            //when
            Page<OfficialAuctionResponse> result = auctionQueryRepository.findOfficialAuctions(seller.getId(),
                    Category.ELECTRONICS, AuctionStatus.PROCEEDING, null, pageable);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("아이패드"); // 가격이 더 높은 아이패드가 먼저
            assertThat(result.getContent().get(1).getAuctionName()).isEqualTo("맥북프로"); // 가격이 낮은 맥북프로가 나중
        }

        @Test
        public void 정식경매_목록_조회_종료까지_남은시간_테스트() throws Exception {
            // given
            Auction auction1 = Auction.builder()
                    .seller(seller)
                    .name("맥북프로")
                    .description("맥북프로 2019년형 팝니다.")
                    .status(AuctionStatus.PROCEEDING)
                    .category(Category.ELECTRONICS)
                    .winnerId(null)
                    .minPrice(1000)
                    .endDateTime(LocalDateTime.now().plusSeconds(3600)) // 1시간 뒤 종료
                    .build();

            Auction auction2 = Auction.builder()
                    .seller(seller)
                    .name("아이패드")
                    .description("아이패드 2019년형 팝니다.")
                    .status(AuctionStatus.PROCEEDING)
                    .category(Category.ELECTRONICS)
                    .winnerId(null)
                    .minPrice(2000)
                    .endDateTime(LocalDateTime.now().plusSeconds(7200)) // 2시간 뒤 종료
                    .build();
            auctionRepository.saveAll(List.of(auction1, auction2));
            Pageable pageable = PageRequest.of(0, 10, Sort.by("immediately"));

            Page<OfficialAuctionResponse> resultWithin1Hour = auctionQueryRepository.findOfficialAuctions(null, null,
                    AuctionStatus.PROCEEDING, 3600, pageable);

            // then
            assertThat(resultWithin1Hour).isNotNull();
            assertThat(resultWithin1Hour.getContent()).hasSize(1);
            assertThat(resultWithin1Hour.getContent().get(0).getAuctionName()).isEqualTo("맥북프로");

            // when - endWithinSeconds 2시간 이내
            Page<OfficialAuctionResponse> resultWithin2Hours = auctionQueryRepository.findOfficialAuctions(
                    seller.getId(),
                    Category.ELECTRONICS,
                    AuctionStatus.PROCEEDING,
                    7200, // 2시간 이내
                    pageable
            );

            // then
            assertThat(resultWithin2Hours).isNotNull();
            assertThat(resultWithin2Hours.getContent()).hasSize(2);
            assertThat(resultWithin2Hours.getContent().get(0).getAuctionName()).isEqualTo("맥북프로"); // 더 빨리 종료되는 맥북
            assertThat(resultWithin2Hours.getContent().get(1).getAuctionName()).isEqualTo("아이패드"); // 나중에 종료되는 아이패드
        }
    }

    @Nested
    @DisplayName("나의 경매 목록 조회")
    class MyAuctions {
        @Test
        void 내가_좋아요한_사전경매_목록조회() {
            // Given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PRE, null, 2000);
            auctionRepository.save(auction1);
            auctionRepository.save(auction2);

            Like like1 = Like.builder().auctionId(auction1.getId()).userId(user.getId()).build();
            Like like2 = Like.builder().auctionId(auction2.getId()).userId(user.getId()).build();
            likeRepository.save(like1);
            likeRepository.save(like2);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // When
            Page<PreAuctionResponse> result = auctionQueryRepository.findLikedAuctionsByUserId(user.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getAuctionName()).isEqualTo("아이패드");
            assertThat(result.getContent().get(1).getAuctionName()).isEqualTo("맥북프로");
        }


        @Test
        void 사용자가_등록한_사전경매_목록_조회() {
            // Given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PRE, null, 1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PRE, null, 2000);
            Auction auction3 = createAuction(user, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PRE, null, 2000);
            auctionRepository.saveAll(List.of(auction1, auction2, auction3));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // When
            Page<PreAuctionResponse> result = auctionQueryRepository.findPreAuctionsByUserId(seller.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            PreAuctionResponse response1 = result.getContent().get(0);
            assertThat(response1.getAuctionName()).isEqualTo("아이패드");
            assertThat(response1.getIsSeller()).isTrue();
            assertThat(response1.getIsLiked()).isFalse();

            PreAuctionResponse response2 = result.getContent().get(1);
            assertThat(response2.getAuctionName()).isEqualTo("맥북프로");
            assertThat(response2.getIsSeller()).isTrue();
            assertThat(response2.getIsLiked()).isFalse();
        }

        @Test
        void 사용자가_등록한_진행중인_경매_목록_조회() {
            // Given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    2000);
            Auction auction3 = createAuction(user, "갤럭시탭", "갤럭시탭 S7 팝니다.", AuctionStatus.PROCEEDING, null, 1500);
            Auction auction4 = createAuction(seller, "종료 아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.ENDED, null, 2000);
            Auction auction5 = createAuction(seller, "사전 아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PRE, null, 2000);
            auctionRepository.saveAll(List.of(auction1, auction2, auction3, auction4, auction5));

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // When
            Page<ProceedingAuctionResponse> result = auctionQueryRepository.findProceedingAuctionsByUserId(
                    seller.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            ProceedingAuctionResponse response1 = result.getContent().get(0);
            assertThat(response1.getAuctionName()).isEqualTo("아이패드");
            assertThat(response1.getIsSeller()).isTrue();

            ProceedingAuctionResponse response2 = result.getContent().get(1);
            assertThat(response2.getAuctionName()).isEqualTo("맥북프로");
            assertThat(response2.getIsSeller()).isTrue();
        }

        @Test
        void 사용자가_등록한_종료된_경매_목록_조회() {
            // Given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, null, 1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.ENDED, user.getId(),
                    2000);
            Auction auction3 = createAuction(user, "갤럭시탭", "갤럭시탭 S7 팝니다.", AuctionStatus.ENDED, null, 1500);
            Auction auction4 = createAuction(seller, "진행중 아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PROCEEDING, null,
                    2000);
            Auction auction5 = createAuction(seller, "사전 아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.PRE, null, 2000);
            auctionRepository.saveAll(List.of(auction1, auction2, auction3, auction4, auction5));

            createOrder(auction2, user, 2000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // When
            Page<EndedAuctionResponse> result = auctionQueryRepository.findEndedAuctionsByUserId(
                    seller.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            EndedAuctionResponse response1 = result.getContent().get(0);
            assertThat(response1.getAuctionName()).isEqualTo("아이패드");
            assertThat(response1.getIsSeller()).isTrue();
            assertThat(response1.getIsWon()).isTrue();
            assertThat(response1.getIsOrdered()).isTrue();

            EndedAuctionResponse response2 = result.getContent().get(1);
            assertThat(response2.getAuctionName()).isEqualTo("맥북프로");
            assertThat(response2.getIsSeller()).isTrue();
            assertThat(response2.getIsWon()).isFalse();
            assertThat(response2.getIsOrdered()).isFalse();
        }

        @Test
        void 사용자가_낙찰한_경매_목록_조회() {
            // given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, user.getId(),
                    1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.ENDED, user.getId(),
                    2000);
            Auction auction3 = createAuction(seller, "갤럭시탭", "갤럭시탭 S7 팝니다.", AuctionStatus.ENDED, null, 1500);
            auctionRepository.saveAll(List.of(auction1, auction2, auction3));

            // 사용자의 입찰 생성
            createBid(user, auction1, 2000L, Bid.BidStatus.ACTIVE);
            createBid(user, auction2, 3000L, Bid.BidStatus.ACTIVE);
            createOrder(auction1, user, 2000L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // When
            Page<WonAuctionResponse> result = auctionQueryRepository.findWonAuctionsByUserId(
                    user.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            WonAuctionResponse response1 = result.getContent().get(0);
            assertThat(response1.getAuctionName()).isEqualTo("아이패드");
            assertThat(response1.getIsOrdered()).isFalse();
            assertThat(response1.getWinningAmount()).isEqualTo(3000L);

            WonAuctionResponse response2 = result.getContent().get(1);
            assertThat(response2.getAuctionName()).isEqualTo("맥북프로");
            assertThat(response2.getIsOrdered()).isTrue();
            assertThat(response2.getWinningAmount()).isEqualTo(2000L);
        }

        @Test
        void 사용자가_낙찰_실패한_경매_목록_조회() {
            // given
            Auction auction1 = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.ENDED, user1.getId(),
                    1000);
            Auction auction2 = createAuction(seller, "아이패드", "아이패드 2021년형 팝니다.", AuctionStatus.ENDED, user1.getId(),
                    2000);
            auctionRepository.saveAll(List.of(auction1, auction2));

            // 사용자의 입찰 생성 (하지만 낙찰되지 않음)
            createBid(user, auction1, 1000L, Bid.BidStatus.ACTIVE);
            createBid(user1, auction1, 2000L, Bid.BidStatus.ACTIVE);
            createBid(user, auction2, 2000L, Bid.BidStatus.ACTIVE);
            createBid(user1, auction2, 3000L, Bid.BidStatus.ACTIVE);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // when
            Page<LostAuctionResponse> result = auctionQueryRepository.findLostAuctionsByUserId(
                    user.getId(), pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            LostAuctionResponse response1 = result.getContent().get(0);
            assertThat(response1.getAuctionName()).isEqualTo("아이패드");

            LostAuctionResponse response2 = result.getContent().get(1);
            assertThat(response2.getAuctionName()).isEqualTo("맥북프로");
        }


    }
}
