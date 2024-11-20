package org.chzz.market.domain.auctionv2.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chzz.market.common.error.GlobalErrorCode.RESOURCE_NOT_FOUND;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.NOW_WINNER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2QueryRepository;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
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
    private AuctionV2Repository auctionV2Repository;

    @Mock
    private AuctionV2QueryRepository auctionV2QueryRepository;

    @Test
    void 낙찰자가_아니거나_낙찰자가_존재하지않는데_조회하면_에러가_발생한다() {
        // given
        AuctionV2 auction = AuctionV2.builder().winnerId(1L).build();
        Long userId = 2L;
        Long auctionId = 1L;

        //when
        when(auctionV2Repository.findById(any())).thenReturn(Optional.of(auction));
        // then
        assertThatThrownBy(() -> auctionWonService.getWinningBidByAuctionId(userId, auctionId))
                .isInstanceOf(AuctionException.class)
                .extracting("errorCode")
                .isEqualTo(NOW_WINNER);
    }

    @Test
    void 예기치못한_에러로_낙찰정보가_조회되지_않으면_에러가_발생한다() {
        // given
        AuctionV2 auction = AuctionV2.builder().winnerId(1L).build();
        Long userId = 1L;
        Long auctionId = 1L;

        //when
        when(auctionV2Repository.findById(any())).thenReturn(Optional.of(auction));
        when(auctionV2QueryRepository.findWinningBidById(any())).thenReturn(Optional.empty());
        // then
        assertThatThrownBy(() -> auctionWonService.getWinningBidByAuctionId(userId, auctionId))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(RESOURCE_NOT_FOUND);
    }

}
