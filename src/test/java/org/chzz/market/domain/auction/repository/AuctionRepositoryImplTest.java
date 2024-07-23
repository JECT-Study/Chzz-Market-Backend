package org.chzz.market.domain.auction.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.entity.SortType;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

@DatabaseTest
@EnableJpaAuditing
@Transactional
class AuctionRepositoryImplTest {

    @Autowired
    AuctionRepository auctionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        User user1 = User.builder().providerId("1234").nickname("닉네임1").email("asd@naver.com").build();
        User user2 = User.builder().providerId("12345").nickname("닉네임2").email("asd1@naver.com").build();

        entityManager.persist(user1);
        entityManager.persist(user2);

        Product product1 = Product.builder().user(user1).name("제품1").category(Category.FASHION).build();
        Product product2 = Product.builder().user(user1).name("제품2").category(Category.BOOK).build();
        Product product3 = Product.builder().user(user2).name("제품3").category(Category.FASHION).build();

        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);

        Auction auction1 = Auction.builder().product(product1).minPrice(1000L).status(Auction.Status.PROCEEDING)
                .build();
        Auction auction2 = Auction.builder().product(product2).minPrice(2000L).status(Auction.Status.PROCEEDING)
                .build();
        Auction auction3 = Auction.builder().product(product3).minPrice(3000L).status(Auction.Status.PROCEEDING)
                .build();

        entityManager.persist(auction1);
        entityManager.persist(auction2);
        entityManager.persist(auction3);

        // 이미지 추가
        Image image1 = Image.builder().product(product1).cdnPath("path/to/image1_1.jpg").build();
        Image image2 = Image.builder().product(product1).cdnPath("path/to/image1_2.jpg").build();
        Image image3 = Image.builder().product(product2).cdnPath("path/to/image2.jpg").build();
        Image image4 = Image.builder().product(product3).cdnPath("path/to/image3.jpg").build();

        entityManager.persist(image1);
        entityManager.persist(image2);
        entityManager.persist(image3);
        entityManager.persist(image4);

        Bid bid1 = Bid.builder().bidder(user1).auction(auction1).amount(2000L).count(3).build();
        Bid bid2 = Bid.builder().bidder(user2).auction(auction1).amount(3000L).count(1).build();
        Bid bid3 = Bid.builder().bidder(user1).auction(auction2).amount(4000L).count(2).build();
        Bid bid4 = Bid.builder().bidder(user2).auction(auction3).amount(5000L).count(4).build();

        entityManager.persist(bid1);
        entityManager.persist(bid2);
        entityManager.persist(bid3);
        entityManager.persist(bid4);
        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        entityManager.createNativeQuery("ALTER TABLE Bid ALTER COLUMN bid_id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE Auction ALTER COLUMN auction_id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE Product ALTER COLUMN product_id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE Image ALTER COLUMN image_id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE Users ALTER COLUMN user_id RESTART WITH 1").executeUpdate();
    }

    @Test
    @DisplayName("특정 카테고리 경매를 높은 가격순으로 조회")
    public void testFindAuctionsByCategoryExpensive() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION, SortType.EXPENSIVE, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(0).getIsParticipating()).isFalse();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image3.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(1).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(2);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
    }

    @Test
    @DisplayName("특정 카테고리 경매를 인기순으로 조회")
    public void testFindAuctionsByCategoryPopularity() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION, SortType.POPULARITY, 2L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(0).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(2);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(1).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image3.jpg");
    }

    @Test
    @DisplayName("경매가 없는 경우 조회")
    public void testFindAuctionsByCategoryNoAuctions() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.TOY, SortType.EXPENSIVE, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}
