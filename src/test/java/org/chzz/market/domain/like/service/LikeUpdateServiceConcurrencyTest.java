package org.chzz.market.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.common.CustomSpringBootTest;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CustomSpringBootTest
public class LikeUpdateServiceConcurrencyTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeUpdateService likeUpdateService;

    @Autowired
    private AuctionRepository auctionRepository;

    private User seller;
    private User user;
    private Image defaultImage;

    @BeforeEach
    void setUp() {
        seller = User.builder().email("seller").providerId("seller").providerType(User.ProviderType.KAKAO).build();
        user = User.builder().email("user").providerId("user").providerType(User.ProviderType.KAKAO).build();
        defaultImage = Image.builder().cdnPath("https://cdn.com").sequence(1).build();
        userRepository.save(seller);
        userRepository.save(user);
    }

    @Test
    public void 좋아요_동시성_테스트() throws InterruptedException {
        Auction auction = createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null);

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // WHEN
        for (int i = 0; i < numberOfThreads; i++) {
            long userId = i + 1;
            executorService.execute(() -> {
                try {
                    likeUpdateService.updateLike(userId, auction.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Auction updatedAuction = auctionRepository.findById(auction.getId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        assertThat(updatedAuction.getLikeCount()).isEqualTo(10);
    }

    @Test
    public void 한사람_동시에_여러_좋아요_요청_테스트() throws InterruptedException {
        // 경매 생성
        Auction auction = createAuction(seller, "아이폰 13", "최신형 아이폰 13 팝니다.", AuctionStatus.PROCEEDING, null);

        int numberOfThreads = 9; // 동시에 요청할 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 하나의 userId를 여러 번 요청
        long userId = user.getId();

        // WHEN
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    likeUpdateService.updateLike(userId, auction.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // THEN
        Auction updatedAuction = auctionRepository.findById(auction.getId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        assertThat(updatedAuction.getLikeCount()).isEqualTo(1);
    }


    private Auction createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId) {
        Auction auction = Auction.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .build();
        auction.addImage(defaultImage);
        auctionRepository.save(auction);
        return auction;
    }
}
