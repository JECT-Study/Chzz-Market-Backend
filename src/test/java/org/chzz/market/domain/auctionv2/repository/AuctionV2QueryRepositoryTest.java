package org.chzz.market.domain.auctionv2.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.repository.UserRepository;
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

    @Test
    void 낙찰정보를_조회한다() {
        // given
        ImageV2 imageV2 = ImageV2.builder().cdnPath("https://cdn.com").sequence(1).build();

        User user = User.builder().email("ex").providerId("ex").providerType(ProviderType.KAKAO).build();
        userRepository.save(user);
        AuctionV2 auction = AuctionV2.builder().seller(user).name("맥북프로").description("맥북프로 2019년형 팝니다.")
                .status(AuctionStatus.PROCEEDING).category(Category.ELECTRONICS).winnerId(1L).build();
        auction.addImage(imageV2);
        Bid bid1 = Bid.builder().bidderId(1L).auctionId(1L).amount(2000L).build();
        Bid bid2 = Bid.builder().bidderId(2L).auctionId(1L).amount(1000L).build();
        auctionV2Repository.save(auction);
        bidRepository.saveAll(List.of(bid1, bid2));
        // when
        Optional<WonAuctionDetailsResponse> result = auctionQueryRepository.findWinningBidById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().winningAmount()).isEqualTo(2000L);
    }

}
