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
import org.chzz.market.common.AWSConfig;
import org.chzz.market.common.CustomSpringBootTest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.dto.request.BidCreateRequest;
import org.chzz.market.domain.bid.error.BidErrorCode;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CustomSpringBootTest
public class BidCreateServiceConcurrencyTest {

    @Autowired
    private BidCreateService bidCreateService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    private Auction auction;
    private User seller;
    private List<User> users;
    private Image defaultImage;

    @BeforeEach
    public void setUp() {
        seller = User.builder().email("seller").providerId("seller").providerType(User.ProviderType.KAKAO).build();
        userRepository.save(seller);
        defaultImage = Image.builder().cdnPath("https://cdn.com").sequence(1).build();
        auction = auctionRepository.save(
                createAuction(seller, "맥북프로", "맥북프로 2019년형 팝니다.", AuctionStatus.PROCEEDING, null));
        users = IntStream.range(1, 6)
                .mapToObj(i -> User.builder()
                        .email("user" + i + "@example.com")
                        .providerId("user" + i)
                        .providerType(User.ProviderType.KAKAO)
                        .build())
                .map(userRepository::save)
                .collect(Collectors.toList());
    }

    @Test
    public void 하나의_경매에_여러명이_입찰할때_동시성테스트() throws InterruptedException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final long userId = users.get(i).getId();
            executorService.execute(() -> {
                try {
                    BidCreateRequest bidRequest = new BidCreateRequest(auction.getId(), 1000L);
                    bidCreateService.create(bidRequest, userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        long bidCount = updatedAuction.getBidCount();
        assertThat(bidCount).isEqualTo(numberOfThreads);
    }

    @Test
    public void 하나의경매에_동일한_사용자가_입찰요청을_할경우_예외가_발생한다() throws InterruptedException {
        int numberOfThreads = 3; // 동일한 사용자가 동시에 요청
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 예외를 수집할 리스트
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    BidCreateRequest bidRequest = new BidCreateRequest(auction.getId(), 1000L);
                    bidCreateService.create(bidRequest, users.get(0).getId());
                } catch (Exception e) {
                    exceptions.add(e); // 예외를 리스트에 추가
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 하나의 성공한 입찰과 두 개의 예외 발생 확인
        assertThat(exceptions).hasSize(numberOfThreads - 1); // 예외는 두 개 발생해야 함
        assertThat(exceptions.get(0))
                .isInstanceOf(BidException.class)
                .extracting("errorCode")
                .isEqualTo(BidErrorCode.BID_SAME_AS_PREVIOUS);

        // 최종 입찰 수 확인 (1번만 성공)
        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        long bidCount = updatedAuction.getBidCount();
        assertThat(bidCount).isEqualTo(1);
    }

    private Auction createAuction(User seller, String name, String description, AuctionStatus status, Long winnerId) {
        Auction auction = Auction.builder()
                .seller(seller)
                .name(name)
                .description(description)
                .status(status)
                .category(Category.ELECTRONICS)
                .winnerId(winnerId)
                .minPrice(1000)
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();
        auction.addImage(defaultImage);
        auctionRepository.save(auction);
        return auction;
    }
}
