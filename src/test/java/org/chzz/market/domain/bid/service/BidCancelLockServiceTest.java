package org.chzz.market.domain.bid.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.error.BidErrorCode;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BidCancelLockServiceTest {

    @Autowired
    private BidCancelLockService bidCancelLockService;

    @Autowired
    private AuctionV2Repository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    private AuctionV2 auction;
    private User seller;
    private List<User> users;
    private List<Bid> bids;
    private ImageV2 defaultImage;

    @BeforeEach
    public void setUp() {
        seller = User.builder().email("seller").providerId("seller").providerType(User.ProviderType.KAKAO).build();
        userRepository.save(seller);
        defaultImage = ImageV2.builder().cdnPath("https://cdn.com").sequence(1).build();
        users = IntStream.range(1, 6)
                .mapToObj(i -> User.builder()
                        .email("user" + i + "@example.com")
                        .providerId("user" + i)
                        .providerType(User.ProviderType.KAKAO)
                        .build())
                .map(userRepository::save)
                .collect(Collectors.toList());
        auction = auctionRepository.save(
                createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null));
        users.forEach(user -> bidRepository.save(
                Bid.builder().auctionId(auction.getId()).bidderId(user.getId()).amount(1000L).build()));
        bids = users.stream()
                .map(user -> Bid.builder()
                        .auctionId(auction.getId())
                        .bidderId(user.getId())
                        .amount(1000L)
                        .build())
                .map(bidRepository::save)
                .collect(Collectors.toList());
    }

    @Test
    public void multipleUsersCancelBidTest() throws InterruptedException {
        int numberOfThreads = users.size();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final long userId = users.get(i).getId();
            final long bidId = bids.get(i).getId();
            executorService.execute(() -> {
                try {
                    bidCancelLockService.cancel(auction.getId(), bidId, userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Auction 업데이트 후 결과 검증
        AuctionV2 updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        long bidCount = updatedAuction.getBidCount();

        // 모든 입찰 취소 후 카운트 0 검증
        assertThat(bidCount).isEqualTo(0);
    }

    @Test
    public void singleUserConcurrentCancelBidTest_ThrowsException() throws InterruptedException {
        int numberOfThreads = 3; // 동일한 사용자가 동시에 요청
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 예외를 수집할 리스트
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            final long userId = users.get(0).getId();
            final long bidId = bids.get(0).getId();
            executorService.execute(() -> {
                try {
                    bidCancelLockService.cancel(auction.getId(), bidId, userId);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 하나의 성공한 요청과 두 개의 예외 발생 확인
        assertThat(exceptions).hasSize(numberOfThreads - 1); // 예외는 2개 발생해야 함
        assertThat(exceptions.get(0))
                .isInstanceOf(BidException.class)
                .extracting("errorCode")
                .isEqualTo(BidErrorCode.BID_ALREADY_CANCELLED);

        // 최종 입찰 수 확인 (4가 되어야 함)
        AuctionV2 updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        long bidCount = updatedAuction.getBidCount();
        assertThat(bidCount).isEqualTo(4);
    }

    private AuctionV2 createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId) {
        AuctionV2 auction = AuctionV2.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .minPrice(1000)
                .bidCount(5L)
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();
        auction.addImage(defaultImage);
        auctionRepository.save(auction);
        return auction;
    }
}
