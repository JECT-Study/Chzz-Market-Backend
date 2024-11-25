package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.common.error.GlobalErrorCode.RESOURCE_NOT_FOUND;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.NOT_WINNER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionWonServiceTest {
    @InjectMocks
    private AuctionWonService auctionWonService;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AuctionQueryRepository auctionQueryRepository;

    @Test
    void 낙찰자가_아니거나_낙찰자가_존재하지않는데_조회하면_에러가_발생한다() {
        // given
        Auction auction = Auction.builder().winnerId(1L).build();
        Long userId = 2L;
        Long auctionId = 1L;

        //when
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        // then
        assertThatThrownBy(() -> auctionWonService.getWinningBidByAuctionId(userId, auctionId))
                .isInstanceOf(AuctionException.class)
                .extracting("errorCode")
                .isEqualTo(NOT_WINNER);
    }

    @Test
    void 예기치못한_에러로_낙찰정보가_조회되지_않으면_에러가_발생한다() {
        // given
        Auction auction = Auction.builder().winnerId(1L).build();
        Long userId = 1L;
        Long auctionId = 1L;

        //when
        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(auctionQueryRepository.findWinningBidById(any())).thenReturn(Optional.empty());
        // then
        assertThatThrownBy(() -> auctionWonService.getWinningBidByAuctionId(userId, auctionId))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(RESOURCE_NOT_FOUND);
    }

}
