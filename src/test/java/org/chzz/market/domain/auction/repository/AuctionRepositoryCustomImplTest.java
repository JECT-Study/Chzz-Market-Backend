package org.chzz.market.domain.auction.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.type.AuctionStatus.ENDED;
import static org.chzz.market.domain.auction.type.AuctionStatus.PROCEEDING;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.auction.dto.BaseAuctionDTO;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@DatabaseTest
class AuctionRepositoryCustomImplTest {

    @Autowired
    AuctionRepository auctionRepository;

    private static User user1, user2, user3, user4;
    private static Product product1, product2, product3, product4, product5, product6, product7;
    private static Auction auction1, auction2, auction3, auction4, auction5, auction6, auction7;

    private static Image image1, image2, image3, image4;
    private static Bid bid1, bid2, bid3, bid4, bid5, bid6;

    @BeforeAll
    static void setUpOnce(@Autowired UserRepository userRepository,
                          @Autowired ProductRepository productRepository,
                          @Autowired AuctionRepository auctionRepository,
                          @Autowired ImageRepository imageRepository,
                          @Autowired BidRepository bidRepository) {
        user1 = User.builder().providerId("1234").nickname("닉네임1").email("asd@naver.com").build();
        user2 = User.builder().providerId("12345").nickname("닉네임2").email("asd1@naver.com").build();
        user3 = User.builder().providerId("123456").nickname("닉네임3").email("asd12@naver.com").build();
        user4 = User.builder().providerId("1234567").nickname("닉네임4").email("asd123@naver.com").build();
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        product1 = Product.builder().user(user1).name("제품1").category(Category.FASHION_AND_CLOTHING).minPrice(10000)
                .build();
        product2 = Product.builder().user(user1).name("제품2").category(Category.BOOKS_AND_MEDIA).minPrice(20000)
                .build();
        product3 = Product.builder().user(user2).name("제품3").category(Category.FASHION_AND_CLOTHING).minPrice(30000)
                .build();
        product4 = Product.builder().user(user2).name("제품4").category(Category.FASHION_AND_CLOTHING).minPrice(40000)
                .build();
        product5 = Product.builder().user(user2).name("제품5").category(Category.ELECTRONICS).minPrice(50000)
                .build();
        product6 = Product.builder().user(user2).name("제품6").category(Category.FURNITURE_AND_INTERIOR).minPrice(60000)
                .build();
        product7 = Product.builder().user(user2).name("제품7").category(Category.SPORTS_AND_LEISURE).minPrice(70000)
                .build();
        productRepository.saveAll(List.of(product1, product2, product3, product4, product5, product6, product7));

        auction1 = Auction.builder().product(product1).status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction2 = Auction.builder().product(product2).status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction3 = Auction.builder().product(product3).status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction4 = Auction.builder().product(product4).status(AuctionStatus.ENDED)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction5 = Auction.builder().product(product5).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusHours(1)).build();
        auction6 = Auction.builder().product(product6).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusSeconds(3000)).build();
        auction7 = Auction.builder().product(product7).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusSeconds(700)).build();
        auctionRepository.saveAll(List.of(auction1, auction2, auction3, auction4, auction5, auction6, auction7));

        image1 = Image.builder().product(product1).cdnPath("path/to/image1_1.jpg").build();
        image2 = Image.builder().product(product1).cdnPath("path/to/image1_2.jpg").build();
        image3 = Image.builder().product(product2).cdnPath("path/to/image2.jpg").build();
        image4 = Image.builder().product(product3).cdnPath("path/to/image3.jpg").build();
        imageRepository.saveAll(List.of(image1, image2, image3, image4));

        bid1 = Bid.builder().bidder(user2).auction(auction1).amount(2000L).build();
        bid2 = Bid.builder().bidder(user2).auction(auction2).amount(4000L).build();
        bid3 = Bid.builder().bidder(user1).auction(auction3).amount(5000L).build();
        bid4 = Bid.builder().bidder(user3).auction(auction2).amount(6000L).build();
        bid5 = Bid.builder().bidder(user1).auction(auction5).amount(7000L).build();
        bid6 = Bid.builder().bidder(user2).auction(auction6).amount(8000L).build();
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4, bid5, bid6));

        auction1.registerBid(bid1);
        auction1.registerBid(bid2);
        auction2.registerBid(bid3);
        auction3.registerBid(bid4);
        auction2.registerBid(bid5);
        auction1.registerBid(bid6);
    }

    @Test
    @DisplayName("특정 카테고리 경매를 높은 가격순으로 조회")
    public void testFindAuctionsByCategoryExpensive() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION_AND_CLOTHING, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(0).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image3.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(1).getIsParticipating()).isFalse();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
    }

    @Test
    @DisplayName("특정 카테고리 경매를 인기순으로 조회")
    public void testFindAuctionsByCategoryPopularity() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("popularity"));

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION_AND_CLOTHING, 2L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(0).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(1).getIsParticipating()).isFalse();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image3.jpg");
    }

    @Test
    @DisplayName("경매가 없는 경우 조회")
    public void testFindAuctionsByCategoryNoAuctions() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.TOYS_AND_HOBBIES, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("경매 상세 조회 - 본인의 제품 경매인 경우")
    public void testFindAuctionDetailsById() throws Exception {
        //given
        Long auctionId = auction1.getId();
        Long userId = user1.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getId());
        assertThat(result.get().getIsSeller()).isTrue();
        assertThat(result.get().getBidAmount()).isEqualTo(0);
        assertThat(result.get().getIsParticipating()).isFalse();
        assertThat(result.get().getBidId()).isNull();
        assertThat(result.get().getImageList()).containsOnly(image1.getCdnPath(), image2.getCdnPath());
    }

    @Test
    @DisplayName("경매 상세 조회 - 다른 사람의 제품 경매 (참여하지 않은 경우)")
    public void testFindAuctionDetailsById_OtherUser_NotParticipating() throws Exception {
        //given
        Long auctionId = auction1.getId();
        Long userId = user3.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getId());
        assertThat(result.get().getIsSeller()).isFalse();
        assertThat(result.get().getBidAmount()).isEqualTo(0);
        assertThat(result.get().getIsParticipating()).isFalse();
        assertThat(result.get().getBidId()).isNull();
    }

    @Test
    @DisplayName("경매 상세 조회 - 다른 사람의 제품 경매 (참여한 경우)")
    public void testFindAuctionDetailsById_OtherUser_Participating() throws Exception {
        //given
        Long auctionId = auction2.getId();
        Long userId = user3.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product2.getId());
        assertThat(result.get().getIsSeller()).isFalse();
        assertThat(result.get().getBidAmount()).isEqualTo(6000L);
        assertThat(result.get().getIsParticipating()).isTrue();
        assertThat(result.get().getBidId()).isEqualTo(bid4.getId());
    }

    @Test
    @DisplayName("경매 상세 조회 - 없는 경매인 경우")
    public void testFindAuctionDetailsById_NonExistentAuction() throws Exception {
        //given
        Long auctionId = 10L;
        Long userId = user1.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("특정 유저의 경매 목록 조회 - 최신순")
    public void testFindMyAuctionsWithNewest() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("newest"));

        //when
        Page<UserAuctionResponse> result = auctionRepository.findAuctionsByNickname(
                user1.getNickname(), pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCreatedAt()).isAfter(result.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 유저의 경매 목록 조회 - 오래된순")
    public void testFindMyAuctionsWithOldest() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("newest")));

        //when
        Page<UserAuctionResponse> result = auctionRepository.findAuctionsByNickname(
                user1.getNickname(), pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCreatedAt()).isBefore(result.getContent().get(1).getCreatedAt());
    }


    @Test
    @DisplayName("나의 경매 목록 조회했는데 없는 경우")
    public void testFindMyAuctionsNotExist() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("newest"));

        //when
        Page<UserAuctionResponse> result = auctionRepository.findAuctionsByNickname(
                user4.getNickname(), pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    @DisplayName("베스트 경매 조회")
    void testFindBestAuctions() {
        // given
        List<AuctionResponse> bestAuctions = auctionRepository.findBestAuctions();
        // when

        // then
        assertThat(bestAuctions).isSortedAccordingTo(
                Comparator.comparingLong(AuctionResponse::getParticipantCount).reversed());
    }

    @Test
    @DisplayName("마감 임박 경매 조회")
    void testImminentAuctions() {
        // given
        List<AuctionResponse> imminentAuctions = auctionRepository.findImminentAuctions();
        // then
        assertThat(imminentAuctions).isNotEmpty();
        assertThat(imminentAuctions.size()).isEqualTo(3);
        assertThat(imminentAuctions).allMatch(auctionResponse -> auctionResponse.getTimeRemaining() <= 3600);
        assertThat(imminentAuctions).isSortedAccordingTo(
                Comparator.comparing(BaseAuctionDTO::getTimeRemaining));

    }

    @Test
    @DisplayName("내가 참여한 경매 목록 조회 -  가격순")
    void testFindParticipatingAuctionRecordWithExpensive() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));
        Page<AuctionResponse> responses = auctionRepository.findParticipatingAuctionRecord(user1.getId(), pageable);
        // when

        // then
        assertThat(responses.getContent())
                .isSortedAccordingTo(Comparator.comparingLong(BaseAuctionDTO::getMinPrice).reversed());
    }

    @Test
    @DisplayName("내가 참여한 경매 목록 조회 -  인기순")
    void testFindParticipatingAuctionRecordWithPopularity() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("popularity"));
        Page<AuctionResponse> responses = auctionRepository.findParticipatingAuctionRecord(user1.getId(), pageable);
        // when

        // then
        assertThat(responses.getContent()).isSortedAccordingTo(
                Comparator.comparingLong(BaseAuctionDTO::getParticipantCount).reversed());
    }

    @Test
    @DisplayName("내가 참여한 경매 목록 조회 -  남은 시간순")
    void testFindParticipatingAuctionRecordWithTime() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("newest"));
        Page<AuctionResponse> responses = auctionRepository.findParticipatingAuctionRecord(user1.getId(), pageable);
        // when

        // then
        assertThat(responses.getContent()).isSortedAccordingTo(
                Comparator.comparingLong(BaseAuctionDTO::getTimeRemaining));
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class getUserProfileTest {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private AuctionRepository auctionRepository;

        @Autowired
        private BidRepository bidRepository;

        User user, seller;
        Product successedProduct, ongoingProduct1, ongoingProduct2, failedProduct1, failedProduct2;

        @BeforeEach
        @Transactional
        void setUp() {
            user = User.builder()
                    .email("test01@gmail.com")
                    .providerId("132456798")
                    .build();
            seller = User.builder()
                    .email("test02@gmail.com")
                    .providerId("222222222")
                    .build();
            userRepository.saveAll(List.of(user, seller));

            successedProduct = Product.builder()
                    .user(seller)
                    .category(Category.BOOKS_AND_MEDIA)
                    .name("product1")
                    .minPrice(10000)
                    .build();
            ongoingProduct1 = Product.builder()
                    .user(seller)
                    .category(Category.OTHER)
                    .name("product2")
                    .minPrice(20000)
                    .build();

            ongoingProduct2 = Product.builder()
                    .user(seller)
                    .category(Category.OTHER)
                    .name("product3")
                    .minPrice(20000)
                    .build();

            failedProduct1 = Product.builder()
                    .user(seller)
                    .category(Category.OTHER)
                    .name("product4")
                    .minPrice(20000)
                    .build();

            failedProduct2 = Product.builder()
                    .user(seller)
                    .category(Category.OTHER)
                    .name("product5")
                    .minPrice(20000)
                    .build();

            productRepository.saveAll(
                    List.of(ongoingProduct1, ongoingProduct2, failedProduct1, failedProduct2, successedProduct));

            Auction ongoingAuction1 = Auction.builder()
                    .endDateTime(LocalDateTime.now().plusHours(1))
                    .product(ongoingProduct1)
                    .status(PROCEEDING)
                    .build();

            Auction ongoingAuction2 = Auction.builder()
                    .endDateTime(LocalDateTime.now().plusHours(1))
                    .product(ongoingProduct2)
                    .status(PROCEEDING)
                    .build();

            Auction failedAuction1 = Auction.builder()
                    .endDateTime(LocalDateTime.now().minusHours(1))
                    .product(failedProduct1)
                    .status(ENDED)
                    .winnerId(2L)
                    .build();

            Auction failedAuction2 = Auction.builder()
                    .endDateTime(LocalDateTime.now().minusHours(1))
                    .product(failedProduct2)
                    .status(ENDED)
                    .winnerId(2L)
                    .build();

            Auction successedAuction = Auction.builder()
                    .endDateTime(LocalDateTime.now().minusHours(1))
                    .product(successedProduct)
                    .status(ENDED)
                    .winnerId(user.getId())
                    .build();
            auctionRepository.saveAll(
                    List.of(ongoingAuction1, ongoingAuction2, failedAuction1, failedAuction2, successedAuction));

            Bid bid1 = Bid.builder()
                    .bidder(user)
                    .auction(successedAuction)
                    .amount(10000L)
                    .build();
            Bid bid2 = Bid.builder()
                    .bidder(user)
                    .auction(failedAuction1)
                    .amount(10000L)
                    .build();
            Bid bid3 = Bid.builder()
                    .bidder(user)
                    .auction(failedAuction2)
                    .amount(10000L)
                    .build();
            Bid bid4 = Bid.builder()
                    .bidder(user)
                    .auction(ongoingAuction1)
                    .amount(1000L)
                    .build();
            Bid bid5 = Bid.builder()
                    .bidder(user)
                    .auction(ongoingAuction2)
                    .amount(1000L)
                    .build();

            bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4, bid5));
        }

        @Test
        @DisplayName("경매 수 정상 조회")
        public void successfulCount() {
            ParticipationCountsResponse counts = auctionRepository.getParticipationCounts(user.getId());
            assertThat(counts).isNotNull();
            assertThat(counts.ongoingAuctionCount()).isEqualTo(2);
            assertThat(counts.successfulAuctionCount()).isEqualTo(1);
            assertThat(counts.failedAuctionCount()).isEqualTo(2);
        }
    }
}
