package org.chzz.market.domain.bid.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.entity.Bid.BidStatus;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(AWSConfig.class)
class BidQueryRepositoryTest {
    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    BidRepository bidRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    BidQueryRepository bidQueryRepository;

    @Test
    void 해당경매_입찰내역을_조회한다() {
        User owner = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        User user1 = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        User user2 = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        User user3 = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        User user4 = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        userRepository.saveAll(List.of(owner, user1, user2, user3, user4));

        Auction auction = Auction.builder().seller(owner).name("맥북프로").description("맥북프로 2019년형 팝니다.")
                .status(AuctionStatus.ENDED).category(Category.ELECTRONICS).winnerId(user1.getId()).build();
        auctionRepository.save(auction);
        Bid bid1 = Bid.builder().bidderId(user1.getId()).auctionId(auction.getId()).amount(2000L)
                .status(BidStatus.ACTIVE).build();
        Bid bid2 = Bid.builder().bidderId(user2.getId()).auctionId(auction.getId()).amount(1000L)
                .status(BidStatus.ACTIVE).build();
        Bid bid3 = Bid.builder().bidderId(user3.getId()).auctionId(auction.getId()).amount(1000L)
                .status(BidStatus.ACTIVE).build();
        Bid bid4 = Bid.builder().bidderId(user4.getId()).auctionId(auction.getId()).amount(3000L)
                .status(BidStatus.CANCELLED).build();
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "bid-amount"));
        Page<BidInfoResponse> result = bidQueryRepository.findBidsByAuctionId(auction.getId(), pageable);
        List<BidInfoResponse> content = result.getContent();

        System.out.println("content = " + content);
        // then
        assertThat(content).hasSize(3);
        assertThat(content).isSortedAccordingTo(
                Comparator.comparing(BidInfoResponse::bidAmount).reversed());
        assertThat(content.get(0).bidAmount()).isEqualTo(2000L);
        assertThat(content.get(0).isWinningBidder()).isTrue();
        assertThat(content.get(1).isWinningBidder()).isFalse();
    }

    @Test
    void 해당경매_입찰내역이_아무것도_없을때_조회한다() {
        User owner = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        userRepository.save(owner);
        Auction auction = Auction.builder().seller(owner).name("맥북프로").description("맥북프로 2019년형 팝니다.")
                .status(AuctionStatus.PROCEEDING).category(Category.ELECTRONICS).winnerId(null).build();
        auctionRepository.save(auction);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "bid-amount"));
        Page<BidInfoResponse> result = bidQueryRepository.findBidsByAuctionId(auction.getId(), pageable);
        List<BidInfoResponse> content = result.getContent();
        // then
        assertThat(content).hasSize(0);
    }
}
