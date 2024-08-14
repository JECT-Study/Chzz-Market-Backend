package org.chzz.market.domain.product.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.product.entity.Product.Category.*;

@DatabaseTest
@Transactional
class ProductRepositoryImplTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    LikeRepository likeRepository;

    @PersistenceContext
    EntityManager entityManager;

    private static User user1, user2, user3;
    private static Product product1, product2, product3, product4, product5;
    private static Image image1, image2, image3, image4, image5;
    private static Like like1, like2, like3;

    @BeforeAll
    static void setUpOnce(@Autowired UserRepository userRepository,
                          @Autowired ProductRepository productRepository,
                          @Autowired ImageRepository imageRepository,
                          @Autowired LikeRepository likeRepository) {
        user1 = User.builder().providerId("1234").nickname("닉네임1").email("user1@test.com").build();
        user2 = User.builder().providerId("5678").nickname("닉네임2").email("user2@test.com").build();
        user3 = User.builder().providerId("9012").nickname("닉네임3").email("user3@test.com").build();
        userRepository.saveAll(List.of(user1, user2, user3));

        product1 = Product.builder().user(user1).name("사전등록상품1").category(ELECTRONICS).minPrice(10000).build();
        product2 = Product.builder().user(user1).name("사전등록상품2").category(BOOKS_AND_MEDIA).minPrice(20000).build();
        product3 = Product.builder().user(user2).name("사전등록상품3").category(ELECTRONICS).minPrice(30000).build();
        product4 = Product.builder().user(user2).name("사전등록상품4").category(ELECTRONICS).minPrice(40000).build();
        product5 = Product.builder().user(user3).name("사전등록상품5").category(FASHION_AND_CLOTHING).minPrice(50000).build();
        productRepository.saveAll(List.of(product1, product2, product3, product4, product5));

        image1 = Image.builder().product(product1).cdnPath("path/to/image1.jpg").build();
        image2 = Image.builder().product(product2).cdnPath("path/to/image2.jpg").build();
        image3 = Image.builder().product(product3).cdnPath("path/to/image3.jpg").build();
        image4 = Image.builder().product(product4).cdnPath("path/to/image4.jpg").build();
        image5 = Image.builder().product(product5).cdnPath("path/to/image5.jpg").build();
        imageRepository.saveAll(List.of(image1, image2, image3, image4, image5));

        like1 = Like.builder().user(user2).product(product1).build();
        like2 = Like.builder().user(user3).product(product1).build();
        like3 = Like.builder().user(user1).product(product3).build();
        likeRepository.saveAll(List.of(like1, like2, like3));
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        imageRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("특정 카테고리 사전 등록 상품을 높은 가격순으로 조회")
    public void testFindProductsByCategoryExpensive() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("product_expensive"));

        // when
        Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(), pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("사전등록상품4");
        assertThat(result.getContent().get(0).getMinPrice()).isEqualTo(40000);
        assertThat(result.getContent().get(1).getName()).isEqualTo("사전등록상품3");
        assertThat(result.getContent().get(1).getMinPrice()).isEqualTo(30000);
        assertThat(result.getContent().get(2).getName()).isEqualTo("사전등록상품1");
        assertThat(result.getContent().get(2).getMinPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("특정 카테고리 사전 등록 상품을 인기순(좋아요 수)으로 조회")
    public void testFindProductsByCategoryPopularity() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("product_popularity"));

        // when
        Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(), pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("사전등록상품1");
        assertThat(result.getContent().get(0).getLikeCount()).isEqualTo(2);
        assertThat(result.getContent().get(1).getName()).isEqualTo("사전등록상품3");
        assertThat(result.getContent().get(1).getLikeCount()).isEqualTo(1);
        assertThat(result.getContent().get(2).getName()).isEqualTo("사전등록상품4");
        assertThat(result.getContent().get(2).getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 카테고리 사전 등록 상품을 최신순으로 조회 (테스트 불가)")
    public void testFindProductsByCategoryNewest() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("product_newest"));

        // when
        Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(), pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        // 여기서는 생성 날짜를 정확히 알 수 없으므로, 순서만 확인합니다.
        assertThat(result.getContent().get(1).getName()).isEqualTo("사전등록상품3");
        assertThat(result.getContent().get(0).getName()).isEqualTo("사전등록상품1");
        assertThat(result.getContent().get(2).getName()).isEqualTo("사전등록상품4");
    }

    @Test
    @DisplayName("페이지네이션 동작 확인")
    public void testPagination() {
        // given
        Pageable firstPage = PageRequest.of(0, 2, Sort.by("product_expensive"));
        Pageable secondPage = PageRequest.of(1, 2, Sort.by("product_expensive"));

        // when
        Page<ProductResponse> firstResult = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(), firstPage);
        Page<ProductResponse> secondResult = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(), secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(firstResult.getContent().get(0).getName()).isEqualTo("사전등록상품4");
        assertThat(firstResult.getContent().get(1).getName()).isEqualTo("사전등록상품3");
        assertThat(secondResult.getContent().get(0).getName()).isEqualTo("사전등록상품1");
    }
}